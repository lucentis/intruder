
package fr.lucentis.intruder.platform.monitoring

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import fr.lucentis.intruder.IntruderApplication
import fr.lucentis.intruder.domain.event.EventCommand
import fr.lucentis.intruder.domain.event.EventProcessingOutcome
import fr.lucentis.intruder.domain.model.EventType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SuccessfulUnlockMonitor : Service() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val pendingCommands = ArrayDeque<EventCommand>()

    private var userPresentReceiver: BroadcastReceiver? = null
    private var processingJob: Job? = null
    private var foregroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        try {
            // Promote the service before registering the receiver.
            if (!foregroundStarted) {
                createNotificationChannel()

                val notification = createNotification()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    val foregroundTypes =
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                            if (
                                checkSelfPermission(Manifest.permission.CAMERA) ==
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                            } else {
                                0
                            }

                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        foregroundTypes
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val foregroundTypes =
                        if (
                            checkSelfPermission(Manifest.permission.CAMERA) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                        } else {
                            0
                        }

                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        foregroundTypes
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                foregroundStarted = true
            }

            registerUserPresentReceiver()

        } catch (exception: Exception) {
            Log.e(TAG, "Unable to start successful unlock monitoring", exception)

            if (foregroundStarted) {
                stopForeground(STOP_FOREGROUND_REMOVE)
                foregroundStarted = false
            }

            stopSelfResult(startId)
            return START_NOT_STICKY
        }

        return START_STICKY
    }

    private fun registerUserPresentReceiver() {
        // Avoid registering the same receiver more than once.
        if (userPresentReceiver != null) return

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent
            ) {
                if (intent.action != Intent.ACTION_USER_PRESENT) return

                // Capture the detection time as soon as Android sends the broadcast.
                val detectedAt = System.currentTimeMillis()

                pendingCommands.addLast(
                    EventCommand(
                        type = EventType.UNLOCK_SUCCESS,
                        detectedAt = detectedAt
                    )
                )

                processNext()
            }
        }

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                receiver,
                filter,
                Context.RECEIVER_EXPORTED
            )
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(receiver, filter)
        }

        userPresentReceiver = receiver

        Log.i(TAG, "Successful unlock receiver registered")
    }

    private fun processNext() {
        // Only one event is processed at a time.
        if (processingJob?.isActive == true) return

        if (pendingCommands.isEmpty()) return

        val command = pendingCommands.removeFirst()

        processingJob = serviceScope.launch {
            try {
                processEvent(command)
            } finally {
                processingJob = null
                processNext()
            }
        }
    }

    private suspend fun processEvent(command: EventCommand) {
        try {
            val container =
                (application as IntruderApplication).container

            when (val outcome = container.eventProcessor.process(command)) {
                is EventProcessingOutcome.Recorded -> {
                    Log.i(
                        TAG,
                        "Successful unlock recorded: id=${outcome.event.id}, " +
                            "captureStatus=${outcome.event.captureStatus}"
                    )
                }

                is EventProcessingOutcome.Ignored -> {
                    Log.i(
                        TAG,
                        "Successful unlock ignored: ${outcome.reason}"
                    )
                }

                is EventProcessingOutcome.PersistenceFailed -> {
                    Log.e(
                        TAG,
                        "Failed to persist successful unlock event. " +
                            "Orphan photo: ${outcome.orphanPhotoLeftBehind}",
                        outcome.cause
                    )
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(
                TAG,
                "Unexpected error while processing successful unlock",
                exception
            )
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Surveillance des déverrouillages",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Surveillance des déverrouillages réussis"
        }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun createNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Intruder")
            .setContentText("Surveillance des déverrouillages en cours")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        userPresentReceiver?.let { receiver ->
            try {
                unregisterReceiver(receiver)
            } catch (exception: Exception) {
                Log.w(TAG, "Unable to unregister successful unlock receiver", exception)
            }
        }

        userPresentReceiver = null

        pendingCommands.clear()
        serviceScope.cancel()

        Log.i(TAG, "Successful unlock monitoring destroyed")

        super.onDestroy()
    }

    companion object {
        private const val TAG = "SuccessfulUnlockMonitor"
        private const val CHANNEL_ID = "successful_unlock_monitor"
        private const val NOTIFICATION_ID = 1002
    }
}

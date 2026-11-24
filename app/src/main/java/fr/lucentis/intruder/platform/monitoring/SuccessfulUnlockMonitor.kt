package fr.lucentis.intruder.platform.monitoring

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
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
import fr.lucentis.intruder.MainActivity
import fr.lucentis.intruder.domain.event.EventCommand
import fr.lucentis.intruder.domain.event.EventProcessingOutcome
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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
            createNotificationChannel()

            // Runs on every start, not only the first one: calling startForeground again
            // refreshes the foreground types, for example once the camera permission has
            // been granted. The notification starts neutral because the real status needs
            // the settings, which are read asynchronously just below.
            startInForeground(createNotification())
            foregroundStarted = true

            registerUserPresentReceiver()

            refreshNotification()

            // A null intent means Android restarted the service after killing the process.
            reportStartup(restartedBySystem = intent == null)

        } catch (exception: Exception) {
            Log.e(TAG, "Unable to start successful unlock monitoring", exception)

            // The service is about to stop, which cancels serviceScope: the alert is posted
            // from the application scope so that it is not cut short.
            val container = (application as IntruderApplication).container

            container.applicationScope.launch {
                container.technicalAlertNotifier.notify(TechnicalAlert.PROTECTION_INTERRUPTED)
            }

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

    // A restart by the system means the monitoring was interrupted. When the cause is a
    // permission that went missing, that more precise alert replaces the generic one.
    private fun reportStartup(restartedBySystem: Boolean) {
        val container = (application as IntruderApplication).container

        serviceScope.launch {
            try {
                val permissionLost = container.permissionWatcher.check(
                    alertOnNewProblem = restartedBySystem
                )

                if (restartedBySystem && !permissionLost) {
                    container.technicalAlertNotifier.notify(
                        TechnicalAlert.PROTECTION_INTERRUPTED
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Unable to report the monitoring startup", exception)
            }
        }
    }

    private fun startInForeground(notification: Notification) {
        val hasCamera =
            checkSelfPermission(Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val foregroundTypes =
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                    if (hasCamera) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0

            startForeground(NOTIFICATION_ID, notification, foregroundTypes)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val foregroundTypes =
                if (hasCamera) ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA else 0

            startForeground(NOTIFICATION_ID, notification, foregroundTypes)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // Replaces the notification text with the real state of the protection.
    private fun refreshNotification() {
        serviceScope.launch {
            try {
                val notification = createNotification(missingPermissions())

                getSystemService(NotificationManager::class.java)
                    .notify(NOTIFICATION_ID, notification)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.w(TAG, "Unable to refresh the monitoring notification", exception)
            }
        }
    }

    // A permission only counts as missing when the feature that needs it is enabled.
    private suspend fun missingPermissions(): List<String> {
        val settings = (application as IntruderApplication)
            .container.settingsRepository.settings.first()

        val missing = mutableListOf<String>()

        if (settings.automaticCaptureEnabled &&
            checkSelfPermission(Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            missing += "caméra"
        }

        val adminActive = getSystemService(DevicePolicyManager::class.java)
            .isAdminActive(ComponentName(this, FailedUnlockMonitor::class.java))

        if (settings.monitorFailedAttempts && !adminActive) {
            missing += "administrateur de l'appareil"
        }

        return missing
    }

    private fun createNotification(
        missingPermissions: List<String> = emptyList()
    ): Notification {
        val (title, text) = when {
            missingPermissions.isEmpty() ->
                "Intruder" to "Surveillance des déverrouillages en cours"

            missingPermissions.size == 1 ->
                "Protection limitée" to
                    "Autorisation manquante : ${missingPermissions.first()}. " +
                    "Appuyez pour ouvrir l'application."

            else ->
                "Protection limitée" to
                    "Autorisations manquantes : ${missingPermissions.joinToString(", ")}. " +
                    "Appuyez pour ouvrir l'application."
        }

        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(text))
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setContentIntent(openApp)
            .setOngoing(true)
            .build()
    }

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
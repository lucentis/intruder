
package fr.lucentis.intruder.platform.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
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

class FailedUnlockCaptureService : Service() {

    private val serviceScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val pendingCommands = ArrayDeque<EventCommand>()

    private var processingJob: Job? = null
    private var lastStartId = 0
    private var foregroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        lastStartId = startId

        // Promote the service before starting any event processing.
        if (!foregroundStarted) {
            try {
                createNotificationChannel()

                startForeground(
                    NOTIFICATION_ID,
                    createNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                )

                foregroundStarted = true
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to start foreground service", exception)
            }
        }

        val detectedAt = intent?.getLongExtra(EXTRA_DETECTED_AT, -1L) ?: -1L

        if (detectedAt < 0L) {
            Log.e(TAG, "Missing or invalid event timestamp")
            stopIfIdle()
            return START_NOT_STICKY
        }

        pendingCommands.addLast(
            EventCommand(
                type = EventType.UNLOCK_FAILED,
                detectedAt = detectedAt
            )
        )

        processNext()

        return START_NOT_STICKY
    }

    private fun processNext() {
        if (processingJob?.isActive == true) return

        if (pendingCommands.isEmpty()) {
            stopIfIdle()
            return
        }

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
                        "Failed unlock recorded: id=${outcome.event.id}, " +
                            "captureStatus=${outcome.event.captureStatus}"
                    )
                }

                is EventProcessingOutcome.Ignored -> {
                    Log.i(TAG, "Failed unlock ignored: ${outcome.reason}")
                }

                is EventProcessingOutcome.PersistenceFailed -> {
                    Log.e(
                        TAG,
                        "Failed to persist unlock event. " +
                            "Orphan photo: ${outcome.orphanPhotoLeftBehind}",
                        outcome.cause
                    )
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e(TAG, "Unexpected error while processing failed unlock", exception)
        }
    }

    private fun stopIfIdle() {
        if (processingJob?.isActive == true || pendingCommands.isNotEmpty()) return

        if (foregroundStarted) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            foregroundStarted = false
        }

        stopSelfResult(lastStartId)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Capture de sécurité",
            NotificationManager.IMPORTANCE_LOW
        )

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun createNotification(): Notification =
        Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("Intruder")
            .setContentText("Traitement d'un événement de sécurité")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setOngoing(true)
            .build()

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "FailedUnlockCaptureService"
        private const val CHANNEL_ID = "intruder_camera"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_DETECTED_AT = "extra_detected_at"
    }
}


package fr.lucentis.intruder.platform.monitoring

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.util.Log
import fr.lucentis.intruder.IntruderApplication
import fr.lucentis.intruder.domain.event.EventCommand
import fr.lucentis.intruder.domain.event.EventProcessingOutcome
import fr.lucentis.intruder.domain.model.EventType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receives Android callbacks related to failed device unlock attempts.
 *
 * Android instantiates this receiver when a relevant device-admin event occurs.
 * It does not need to run as a permanent service.
 */
class FailedUnlockMonitor : DeviceAdminReceiver() {

    override fun onPasswordFailed(
        context: Context,
        intent: Intent,
        user: UserHandle
    ) {
        // Keep the broadcast alive while the event is processed asynchronously.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container =
                    (context.applicationContext as IntruderApplication).container

                val command = EventCommand(
                    type = EventType.UNLOCK_FAILED,
                    detectedAt = System.currentTimeMillis()
                )

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
            } finally {
                // Always release the broadcast, even if processing fails.
                pendingResult.finish()
            }
        }
    }

    private companion object {
        const val TAG = "FailedUnlockMonitor"
    }
}

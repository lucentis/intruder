package fr.lucentis.intruder.platform.monitoring

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import fr.lucentis.intruder.IntruderApplication
import fr.lucentis.intruder.di.AppContainer
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Receives Android callbacks related to failed device unlock attempts.
 *
 * It delegates event processing to a foreground service.
 */
class FailedUnlockMonitor : DeviceAdminReceiver() {

    override fun onPasswordFailed(
        context: Context,
        intent: Intent,
        user: android.os.UserHandle
    ) {
        // Capture the detection time before starting the service.
        val detectedAt = System.currentTimeMillis()

        val serviceIntent = Intent(
            context,
            FailedUnlockCaptureService::class.java
        ).putExtra(
            FailedUnlockCaptureService.EXTRA_DETECTED_AT,
            detectedAt
        )

        try {
            context.startForegroundService(serviceIntent)

            Log.i(TAG, "Failed unlock capture service started")
        } catch (exception: Exception) {
            Log.e(TAG, "Unable to start failed unlock capture service", exception)

            // The failed attempt will not be recorded: the protection is not doing its job.
            runAsync(context) { container ->
                container.technicalAlertNotifier.notify(TechnicalAlert.PROTECTION_INTERRUPTED)
            }
        }
    }

    // Android calls this when the user removes the device administrator, which is what
    // makes failed attempts detectable.
    override fun onDisabled(context: Context, intent: Intent) {
        runAsync(context) { container ->
            container.permissionWatcher.onAdminRemoved()
        }
    }

    // A receiver is destroyed as soon as its callback returns. goAsync keeps it alive until
    // finish() is called, so the work can run on a coroutine without being cut short.
    private fun runAsync(
        context: Context,
        block: suspend (AppContainer) -> Unit
    ) {
        val container = (context.applicationContext as IntruderApplication).container
        val pending = goAsync()

        container.applicationScope.launch {
            try {
                block(container)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Log.e(TAG, "Unable to report a technical problem", exception)
            } finally {
                pending?.finish()
            }
        }
    }

    private companion object {
        const val TAG = "FailedUnlockMonitor"
    }
}
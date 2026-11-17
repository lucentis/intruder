
package fr.lucentis.intruder.platform.monitoring

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

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
        }
    }

    private companion object {
        const val TAG = "FailedUnlockMonitor"
    }
}

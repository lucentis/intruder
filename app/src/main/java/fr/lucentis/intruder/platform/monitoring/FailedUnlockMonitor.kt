
package fr.lucentis.intruder.platform.monitoring

import android.app.admin.DeviceAdminReceiver

/**
 * Receives Android callbacks related to failed device unlock attempts.
 *
 * Android instantiates this receiver when a relevant device-admin event occurs.
 * It does not need to run as a permanent service.
 */
class FailedUnlockMonitor : DeviceAdminReceiver()

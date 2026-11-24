package fr.lucentis.intruder.platform.monitoring

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import fr.lucentis.intruder.domain.notification.AlertScreen
import fr.lucentis.intruder.domain.notification.AlertTarget
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import fr.lucentis.intruder.domain.notification.TechnicalAlertNotifier
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.first

// Detects the moment a permission needed by an enabled feature goes missing, and reports it
// once. The set of missing permissions seen at the previous check is persisted, because the
// process can be killed at any time (revoking the camera permission kills it).
class ProtectionPermissionWatcher(
    context: Context,
    private val settingsRepository: SettingsRepository,
    private val notifier: TechnicalAlertNotifier
) {

    private val appContext = context.applicationContext

    private val preferences = appContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    // Records the current state and returns true when a permission went missing since the
    // previous check. The first check ever only records the state: a permission that was
    // never granted is not a revocation. Foreground callers pass alertOnNewProblem = false,
    // since the user is already looking at the app.
    suspend fun check(alertOnNewProblem: Boolean): Boolean {
        val missing = currentlyMissing()
        val previous = preferences.getStringSet(KEY_MISSING, null)

        preferences.edit().putStringSet(KEY_MISSING, missing).apply()

        val newlyMissing = if (previous == null) emptySet() else missing - previous
        val isNewProblem = newlyMissing.isNotEmpty()

        if (isNewProblem && alertOnNewProblem) {
            notifier.notify(TechnicalAlert.MISSING_PERMISSION, targetFor(newlyMissing))
        }

        return isNewProblem
    }

    // Called when Android tells us the device administrator was removed. At that moment the
    // administrator state read from the system is not reliable, so it is recorded directly.
    suspend fun onAdminRemoved() {
        val settings = settingsRepository.settings.first()

        if (!settings.globalProtectionEnabled || !settings.monitorFailedAttempts) return

        val previous = preferences.getStringSet(KEY_MISSING, emptySet()).orEmpty()

        if (ADMIN in previous) return

        preferences.edit().putStringSet(KEY_MISSING, previous + ADMIN).apply()

        notifier.notify(
            TechnicalAlert.MISSING_PERMISSION,
            AlertTarget(AlertScreen.SETTINGS_MONITORING)
        )
    }

    // The page that shows the warning for the lost permission. When both were lost, the
    // global protection page lists the state of every feature.
    private fun targetFor(newlyMissing: Set<String>): AlertTarget {
        val screen = when {
            newlyMissing.size > 1 -> AlertScreen.SETTINGS_GLOBAL_PROTECTION
            ADMIN in newlyMissing -> AlertScreen.SETTINGS_MONITORING
            else -> AlertScreen.SETTINGS_PHOTO_CAPTURE
        }

        return AlertTarget(screen)
    }

    // A permission only counts when the feature that needs it is enabled.
    private suspend fun currentlyMissing(): Set<String> {
        val settings = settingsRepository.settings.first()

        if (!settings.globalProtectionEnabled) return emptySet()

        val missing = mutableSetOf<String>()

        val cameraGranted = appContext.checkSelfPermission(Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

        if (settings.automaticCaptureEnabled && !cameraGranted) {
            missing += CAMERA
        }

        val adminActive = appContext.getSystemService(DevicePolicyManager::class.java)
            .isAdminActive(ComponentName(appContext, FailedUnlockMonitor::class.java))

        if (settings.monitorFailedAttempts && !adminActive) {
            missing += ADMIN
        }

        return missing
    }

    private companion object {
        const val PREFERENCES_NAME = "permission_watch"
        const val KEY_MISSING = "missing"
        const val CAMERA = "camera"
        const val ADMIN = "admin"
    }
}
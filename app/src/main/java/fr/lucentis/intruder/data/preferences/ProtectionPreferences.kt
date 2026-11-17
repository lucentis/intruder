package fr.lucentis.intruder.data.preferences

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.PhotoQuality
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// DataStore replaces SharedPreferences: asynchronous, safe for the main thread, observable
// through Flow, and writes are atomic.
// There must be exactly one instance per file in the process (a second one crashes). The
// delegate is therefore declared once, at top level of this file, and kept private.
private val Context.protectionDataStore by preferencesDataStore(
    name = "protection_preferences",
    // A corrupted file is replaced by empty preferences (all defaults) instead of making
    // every later read and write fail. Note that this also turns the global protection off.
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

// These values are what the user asked for. They are not proof that the services are
// actually running: the real state is checked separately.
class ProtectionPreferences(context: Context) : SettingsRepository {

    private val dataStore = context.applicationContext.protectionDataStore

    override val settings: Flow<AppSettings> = dataStore.data
        // Reading can fail with an IOException (disk error). Fall back to the defaults so
        // that callers, EventProcessor first, never lose an event because of it. The flow
        // completes after the fallback value: a screen would need to collect again.
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { it.toSettings() }

    // edit runs the block as one atomic transaction on the stored preferences.
    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            preferences.writeSettings(transform(preferences.toSettings()))
        }
    }
}

private val GLOBAL_PROTECTION = booleanPreferencesKey("global_protection_enabled")
private val MONITOR_SUCCESSFUL_UNLOCKS = booleanPreferencesKey("monitor_successful_unlocks")
private val MONITOR_FAILED_ATTEMPTS = booleanPreferencesKey("monitor_failed_attempts")
private val AUTOMATIC_CAPTURE = booleanPreferencesKey("automatic_capture_enabled")
private val CAMERA_FACING = stringPreferencesKey("camera_facing")
private val PHOTO_QUALITY = stringPreferencesKey("photo_quality")
private val NOTIFY_PROTECTION_INTERRUPTED = booleanPreferencesKey("notify_protection_interrupted")
private val NOTIFY_MISSING_PERMISSION = booleanPreferencesKey("notify_missing_permission")
private val NOTIFY_CAPTURE_PROBLEM = booleanPreferencesKey("notify_capture_problem")

// A key that was never written falls back to the default value of AppSettings.
private fun Preferences.toSettings(): AppSettings {
    val defaults = AppSettings()

    return AppSettings(
        globalProtectionEnabled = this[GLOBAL_PROTECTION] ?: defaults.globalProtectionEnabled,
        monitorSuccessfulUnlocks =
            this[MONITOR_SUCCESSFUL_UNLOCKS] ?: defaults.monitorSuccessfulUnlocks,
        monitorFailedAttempts = this[MONITOR_FAILED_ATTEMPTS] ?: defaults.monitorFailedAttempts,
        automaticCaptureEnabled = this[AUTOMATIC_CAPTURE] ?: defaults.automaticCaptureEnabled,
        cameraFacing = enumOrDefault(this[CAMERA_FACING], defaults.cameraFacing),
        photoQuality = enumOrDefault(this[PHOTO_QUALITY], defaults.photoQuality),
        notifyProtectionInterrupted =
            this[NOTIFY_PROTECTION_INTERRUPTED] ?: defaults.notifyProtectionInterrupted,
        notifyMissingPermission =
            this[NOTIFY_MISSING_PERMISSION] ?: defaults.notifyMissingPermission,
        notifyCaptureProblem = this[NOTIFY_CAPTURE_PROBLEM] ?: defaults.notifyCaptureProblem
    )
}

private fun MutablePreferences.writeSettings(settings: AppSettings) {
    this[GLOBAL_PROTECTION] = settings.globalProtectionEnabled
    this[MONITOR_SUCCESSFUL_UNLOCKS] = settings.monitorSuccessfulUnlocks
    this[MONITOR_FAILED_ATTEMPTS] = settings.monitorFailedAttempts
    this[AUTOMATIC_CAPTURE] = settings.automaticCaptureEnabled
    this[CAMERA_FACING] = settings.cameraFacing.name
    this[PHOTO_QUALITY] = settings.photoQuality.name
    this[NOTIFY_PROTECTION_INTERRUPTED] = settings.notifyProtectionInterrupted
    this[NOTIFY_MISSING_PERMISSION] = settings.notifyMissingPermission
    this[NOTIFY_CAPTURE_PROBLEM] = settings.notifyCaptureProblem
}

// reified lets the function read the enum's values at runtime: enumValues<T>().
private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
    enumValues<T>().firstOrNull { it.name == name } ?: default
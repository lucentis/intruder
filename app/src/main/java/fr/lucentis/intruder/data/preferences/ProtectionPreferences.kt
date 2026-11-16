package fr.lucentis.intruder.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore replaces SharedPreferences: asynchronous, safe for the main thread, observable
// through Flow, and writes are atomic.
// There must be exactly one instance per file in the process (a second one crashes). The
// delegate is therefore declared once, at top level of this file, and kept private.
private val Context.protectionDataStore by preferencesDataStore(
    name = "protection_preferences"
)

// These values are what the user asked for. They are not proof that the services are
// actually running: the real state is checked separately.
class ProtectionPreferences(context: Context) {

    private val dataStore = context.applicationContext.protectionDataStore

    // Emits the current value, then again after every change.
    // Off until the user confirms the activation wizard
    val globalProtectionEnabled: Flow<Boolean> = dataStore.data
        .map { it[KEY_GLOBAL_PROTECTION] ?: false }

    suspend fun setGlobalProtectionEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_GLOBAL_PROTECTION] = enabled }
    }

    private companion object {
        val KEY_GLOBAL_PROTECTION =
            booleanPreferencesKey("global_protection_enabled")
    }
}
package fr.lucentis.intruder.domain.settings

import fr.lucentis.intruder.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

// Contract implemented by the data layer (DataStore). The domain and the ViewModels only
// know this interface, which is what lets tests replace it with an in-memory fake.
interface SettingsRepository {

    // Emits the current settings, then again after every change.
    val settings: Flow<AppSettings>

    // Atomic read-modify-write, e.g. update { it.copy(automaticCaptureEnabled = false) }.
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
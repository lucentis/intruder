package fr.lucentis.intruder.data.preferences

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.domain.gallery.GalleryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.galleryDataStore by preferencesDataStore(
    name = "gallery_preferences",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

class GalleryPreferences(context: Context) : GalleryRepository {

    private val dataStore = context.applicationContext.galleryDataStore

    override val displayMode: Flow<GalleryDisplayMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences.toGalleryDisplayMode()
        }

    override suspend fun setDisplayMode(mode: GalleryDisplayMode) {
        dataStore.edit { preferences ->
            preferences[DISPLAY_MODE] = mode.name
        }
    }
}

private val DISPLAY_MODE = stringPreferencesKey("display_mode")

private fun Preferences.toGalleryDisplayMode(): GalleryDisplayMode {
    val storedMode = this[DISPLAY_MODE]

    return GalleryDisplayMode.entries
        .firstOrNull { it.name == storedMode }
        ?: GalleryDisplayMode.GRID
}

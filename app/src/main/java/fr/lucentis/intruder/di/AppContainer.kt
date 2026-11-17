package fr.lucentis.intruder.di

import android.content.Context
import fr.lucentis.intruder.data.database.IntruderDatabase
import fr.lucentis.intruder.data.preferences.ProtectionPreferences
import fr.lucentis.intruder.data.repository.FilesystemPhotoStorage
import fr.lucentis.intruder.data.repository.RoomEventRepository
import fr.lucentis.intruder.domain.event.EventProcessor
import fr.lucentis.intruder.domain.event.EventRepository
import fr.lucentis.intruder.domain.event.PhotoStorage
import fr.lucentis.intruder.domain.settings.SettingsRepository
import fr.lucentis.intruder.platform.camera.AndroidPhotoCapturer
import java.io.File

/**
 * Manual dependency injection: one place that builds the shared objects. Services and
 * receivers will get theirs from here through the Application instance. The properties
 * are typed with the domain interfaces: callers never see Room or DataStore.
 */
class AppContainer(context: Context) {

    // A Context is the handle to the Android system (files, resources, system services).
    // Activities, Services and the Application are all Contexts. Long-lived objects must
    // keep the application context: holding an Activity would leak it after it is destroyed.
    private val appContext = context.applicationContext

    // filesDir is the app's private storage. The camera layer will save photos here.
    val photosDirectory: File = File(appContext.filesDir, "captures")

    // by lazy: built on first access, so the app does not pay for the database and the
    // preferences at process start. Room only opens the file on the first query anyway.
    val database: IntruderDatabase by lazy {
        IntruderDatabase.create(appContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        ProtectionPreferences(appContext)
    }

    val photoStorage: PhotoStorage by lazy {
        FilesystemPhotoStorage(photosDirectory)
    }

    val eventRepository: EventRepository by lazy {
        RoomEventRepository(database.eventDao(), photoStorage)
    }
    
    private val photoCapturer by lazy {
        AndroidPhotoCapturer(
            context = appContext,
            photosDirectory = photosDirectory
        )
    }

    val eventProcessor: EventProcessor by lazy {
        EventProcessor(
            settingsRepository = settingsRepository,
            photoCapturer = photoCapturer,
            eventRepository = eventRepository,
            photoStorage = photoStorage
        )
    }
}
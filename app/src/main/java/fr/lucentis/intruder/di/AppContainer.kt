package fr.lucentis.intruder.di

import android.content.Context
import fr.lucentis.intruder.data.database.IntruderDatabase
import fr.lucentis.intruder.data.preferences.ProtectionPreferences

/**
 * Manual dependency injection: one place that builds the shared objects. Services and
 * receivers will get theirs from here through the Application instance.
 */
class AppContainer(context: Context) {

    // A Context is the handle to the Android system (files, resources, system services).
    // Activities, Services and the Application are all Contexts. Long-lived objects must
    // keep the application context: holding an Activity would leak it after it is destroyed.
    private val appContext = context.applicationContext

    // by lazy: built on first access, so the app does not pay for the database and the
    // preferences at process start. Room only opens the file on the first query anyway.
    val database: IntruderDatabase by lazy {
        IntruderDatabase.create(appContext)
    }

    val protectionPreferences: ProtectionPreferences by lazy {
        ProtectionPreferences(appContext)
    }
}
package fr.lucentis.intruder

import android.app.Application
import fr.lucentis.intruder.di.AppContainer

/**
 * One instance per app process, created by the system before any Activity, Service or
 * BroadcastReceiver. It is the natural place to build objects shared by all components.
 *
 * Android may kill the process whenever the app is in the background. The process is
 * recreated later and onCreate runs again with an empty memory: anything that is not
 * persisted (Room, DataStore, files) is lost. Never treat memory as durable state.
 */
class IntruderApplication : Application() {

    // lateinit: assigned in onCreate, which the system guarantees to run before use.
    // private set: only this class can assign it, other components just read it.
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
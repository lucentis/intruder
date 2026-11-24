package fr.lucentis.intruder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import fr.lucentis.intruder.domain.notification.AlertTarget
import fr.lucentis.intruder.platform.notification.AlertIntent
import fr.lucentis.intruder.presentation.navigation.AppNavigation
import fr.lucentis.intruder.presentation.theme.IntruderTheme

/**
 * An Activity is a screen entry point that the system creates and destroys through a
 * lifecycle (onCreate, onStart, onResume, ..., onDestroy). Android can destroy and
 * recreate it at any time (rotation, theme change, low memory), so important state must
 * never live in its fields: use Room, DataStore or a ViewModel.
 *
 * Here it hosts the Compose UI and hands it the destination of the notification that
 * opened the app, if any.
 */
class MainActivity : ComponentActivity() {

    // The destination asked by a notification, until the UI has navigated to it. It is
    // Compose state so that setting it from onNewIntent updates the running screen.
    private var openRequest by mutableStateOf<AlertTarget?>(null)

    // Called when the system creates the Activity, and again after each recreation.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // After a recreation (rotation, process death) the intent is still the one that
        // first started the Activity. Reading it again would send the user back to the
        // notification's page.
        if (savedInstanceState == null) {
            openRequest = AlertIntent.parse(intent)
        }

        // "application" is the Application instance declared in the manifest, shared by the
        // whole process, hence by services and receivers too.
        val container = (application as IntruderApplication).container

        // setContent replaces the old XML layouts: the lambda describes the UI in Compose.
        setContent {
            IntruderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        container = container,
                        openRequest = openRequest,
                        onOpenRequestHandled = { openRequest = null }
                    )
                }
            }
        }
    }

    // With launchMode="singleTop", tapping a notification while the app is already open
    // delivers the new intent here instead of creating a second Activity.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        setIntent(intent)
        openRequest = AlertIntent.parse(intent)
    }
}
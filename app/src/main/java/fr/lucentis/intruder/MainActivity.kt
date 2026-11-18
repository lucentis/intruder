package fr.lucentis.intruder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import fr.lucentis.intruder.di.AppContainer
import fr.lucentis.intruder.presentation.navigation.AppNavigation

/**
 * An Activity is a screen entry point that the system creates and destroys through a
 * lifecycle (onCreate, onStart, onResume, ..., onDestroy). Android can destroy and
 * recreate it at any time (rotation, theme change, low memory), so important state must
 * never live in its fields: use Room, DataStore or a ViewModel.
 *
 * Here it only hosts the Compose UI.
 */
class MainActivity : ComponentActivity() {

    // Called when the system creates the Activity, and again after each recreation.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // "application" is the Application instance declared in the manifest, shared by the
        // whole process, hence by services and receivers too.
        val container = (application as IntruderApplication).container

        // setContent replaces the old XML layouts: the lambda describes the UI in Compose.
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(container)
                }
            }
        }
    }
}

package fr.lucentis.intruder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.di.AppContainer
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import kotlinx.coroutines.launch

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
                    SkeletonScreen(container)
                }
            }
        }
    }
}

// Temporary screen used to validate the toolchain, the repositories and DataStore on a
// device. It goes through the domain interfaces, like the real screens will.
//
// A @Composable function describes the UI for a given state. Compose calls it again
// ("recomposition") whenever a State it read has changed, and redraws only what differs.
@Composable
private fun SkeletonScreen(container: AppContainer) {
    // Coroutine scope tied to this composable, cancelled when it leaves the screen. Click
    // callbacks are not suspend functions, so coroutines are launched from this scope.
    val scope = rememberCoroutineScope()

    // remember: keeps the value across recompositions (not across an Activity recreation).
    //   Without it, a new Flow would be created at every recomposition.
    // collectAsState: exposes the latest value of a Flow as a Compose State. When the table
    //   changes, Room emits, the state updates and the screen recomposes by itself.
    // "by": delegated property, so we write eventCount instead of eventCount.value.
    val eventCount by remember { container.eventRepository.observeCount() }
        .collectAsState(initial = 0)

    val settings by remember { container.settingsRepository.settings }
        .collectAsState(initial = AppSettings())

    // mutableStateOf creates a State that the UI can write to: changing it recomposes.
    var lastTestEventId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // With targetSdk 35 and above, Android draws apps edge to edge, under the status
            // bar and the navigation bar. This adds padding equal to the size of those bars.
            .systemBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Intruder",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Squelette technique, phase 2.",
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Base Room : $eventCount événement(s) enregistré(s).",
            modifier = Modifier.padding(top = 24.dp)
        )

        Text(
            text = if (settings.globalProtectionEnabled) {
                "Préférences DataStore : protection globale activée."
            } else {
                "Préférences DataStore : protection globale désactivée."
            },
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Photos automatiques : ${if (settings.automaticCaptureEnabled) "oui" else "non"}, " +
                "caméra : ${settings.cameraFacing}, qualité : ${settings.photoQuality}.",
            modifier = Modifier.padding(top = 8.dp)
        )

        Button(
            onClick = {
                scope.launch {
                    lastTestEventId = container.eventRepository.insert(
                        Event(
                            type = EventType.UNLOCK_SUCCESS,
                            timestamp = System.currentTimeMillis(),
                            photoPath = null,
                            captureStatus = CaptureStatus.SKIPPED,
                            failureReason = null
                        )
                    )
                }
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Ajouter un événement de test")
        }

        Button(
            onClick = {
                // A delegated property cannot be smart-cast, hence the local copy.
                val id = lastTestEventId
                if (id != null) {
                    scope.launch {
                        container.eventRepository.deleteEvents(listOf(id))
                        lastTestEventId = null
                    }
                }
            },
            enabled = lastTestEventId != null,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Supprimer le dernier événement de test")
        }

        Button(
            onClick = {
                scope.launch {
                    container.settingsRepository.update {
                        it.copy(globalProtectionEnabled = !it.globalProtectionEnabled)
                    }
                }
            },
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Basculer la protection globale")
        }
    }
}
package fr.lucentis.intruder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

/**
 * Main entry point of the application.
 *
 * ComponentActivity is an Android Activity that integrates with Jetpack libraries,
 * including Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called by Android when this Activity is created.
     *
     * savedInstanceState can contain previously saved state when Android
     * recreates the Activity, for example after a configuration change.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent creates the Compose UI instead of using an XML layout.
        setContent {
            // MaterialTheme provides Material Design styling and default values.
            MaterialTheme {
                // Surface is a container that provides a background and visual surface.
                Surface {
                    // Text is a basic composable that displays text on screen.
                    Text("Intruder")
                }
            }
        }
    }
}


package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.runtime.Composable

@Composable
fun PhotoCaptureScreen(onBack: () -> Unit) {
    SettingsPlaceholderScreen(
        title = "Capture photo",
        onBack = onBack
    )
}

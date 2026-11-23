
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.runtime.Composable

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SettingsPlaceholderScreen(
        title = "À propos",
        onBack = onBack
    )
}

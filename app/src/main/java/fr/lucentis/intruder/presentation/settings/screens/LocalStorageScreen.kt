
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.runtime.Composable

@Composable
fun LocalStorageScreen(onBack: () -> Unit) {
    SettingsPlaceholderScreen(
        title = "Stockage local",
        onBack = onBack
    )
}


package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.runtime.Composable

@Composable
fun TechnicalNotificationsScreen(onBack: () -> Unit) {
    SettingsPlaceholderScreen(
        title = "Notifications techniques",
        onBack = onBack
    )
}

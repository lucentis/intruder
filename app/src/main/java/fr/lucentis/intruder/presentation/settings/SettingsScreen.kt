
package fr.lucentis.intruder.presentation.settings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import fr.lucentis.intruder.domain.model.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleGlobalProtection: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit
) {
    val navController = rememberNavController()

    SettingsNavigation(
        navController = navController,
        settings = settings,
        onToggleGlobalProtection = onToggleGlobalProtection,
        onToggleFailedAttempts = onToggleFailedAttempts,
        onToggleSuccessfulUnlocks = onToggleSuccessfulUnlocks
    )
}


package fr.lucentis.intruder.presentation.settings

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.presentation.settings.screens.AboutScreen
import fr.lucentis.intruder.presentation.settings.screens.GlobalProtectionScreen
import fr.lucentis.intruder.presentation.settings.screens.LocalStorageScreen
import fr.lucentis.intruder.presentation.settings.screens.MonitoringScreen
import fr.lucentis.intruder.presentation.settings.screens.PhotoCaptureScreen
import fr.lucentis.intruder.presentation.settings.screens.SettingsHomeScreen
import fr.lucentis.intruder.presentation.settings.screens.TechnicalNotificationsScreen

@Composable
fun SettingsNavigation(
    navController: NavHostController,
    settings: AppSettings,
    onToggleGlobalProtection: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = SettingsDestination.HOME.route
    ) {
        composable(SettingsDestination.HOME.route) {
            SettingsHomeScreen(
                globalProtectionEnabled = settings.globalProtectionEnabled,
                onNavigate = { destination ->
                    navController.navigate(destination.route)
                }
            )
        }

        composable(SettingsDestination.GLOBAL_PROTECTION.route) {
            GlobalProtectionScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onToggleGlobalProtection = onToggleGlobalProtection
            )
        }

        composable(SettingsDestination.MONITORING.route) {
            MonitoringScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onToggleFailedAttempts = onToggleFailedAttempts,
                onToggleSuccessfulUnlocks = onToggleSuccessfulUnlocks
            )
        }

        composable(SettingsDestination.PHOTO_CAPTURE.route) {
            PhotoCaptureScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsDestination.TECHNICAL_NOTIFICATIONS.route) {
            TechnicalNotificationsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsDestination.LOCAL_STORAGE.route) {
            LocalStorageScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsDestination.ABOUT.route) {
            AboutScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

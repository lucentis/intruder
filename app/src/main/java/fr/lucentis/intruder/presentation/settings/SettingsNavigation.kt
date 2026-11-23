
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
    isAdminActive: Boolean,
    hasCameraPermission: Boolean,
    onActivateAdmin: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onToggleGlobalProtection: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit,
    onToggleAutomaticCapture: () -> Unit,
    onToggleProtectionInterrupted: () -> Unit,
    onToggleMissingPermission: () -> Unit,
    onToggleCaptureProblem: () -> Unit
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
                isAdminActive = isAdminActive,
                onBack = { navController.popBackStack() },
                onActivateAdmin = onActivateAdmin,
                onToggleFailedAttempts = onToggleFailedAttempts,
                onToggleSuccessfulUnlocks = onToggleSuccessfulUnlocks
            )
        }

        composable(SettingsDestination.PHOTO_CAPTURE.route) {
            PhotoCaptureScreen(
                settings = settings,
                hasCameraPermission = hasCameraPermission,
                onBack = { navController.popBackStack() },
                onRequestCameraPermission = onRequestCameraPermission,
                onToggleAutomaticCapture = onToggleAutomaticCapture
            )
        }

        composable(SettingsDestination.TECHNICAL_NOTIFICATIONS.route) {
            TechnicalNotificationsScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onToggleProtectionInterrupted = onToggleProtectionInterrupted,
                onToggleMissingPermission = onToggleMissingPermission,
                onToggleCaptureProblem = onToggleCaptureProblem
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

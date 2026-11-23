package fr.lucentis.intruder.presentation.settings

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import fr.lucentis.intruder.domain.model.AppSettings

@Composable
fun SettingsScreen(
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
    val navController = rememberNavController()

    SettingsNavigation(
        navController = navController,
        settings = settings,
        isAdminActive = isAdminActive,
        hasCameraPermission = hasCameraPermission,
        onActivateAdmin = onActivateAdmin,
        onRequestCameraPermission = onRequestCameraPermission,
        onToggleGlobalProtection = onToggleGlobalProtection,
        onToggleFailedAttempts = onToggleFailedAttempts,
        onToggleSuccessfulUnlocks = onToggleSuccessfulUnlocks,
        onToggleAutomaticCapture = onToggleAutomaticCapture,
        onToggleProtectionInterrupted = onToggleProtectionInterrupted,
        onToggleMissingPermission = onToggleMissingPermission,
        onToggleCaptureProblem = onToggleCaptureProblem
    )
}
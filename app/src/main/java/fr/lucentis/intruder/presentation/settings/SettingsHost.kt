package fr.lucentis.intruder.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import fr.lucentis.intruder.domain.model.AppSettings

enum class SettingsRoute {
    MAIN,
    GLOBAL_PROTECTION,
    MONITORING,
    PHOTO_CAPTURE,
    NOTIFICATIONS,
    STORAGE,
    ABOUT
}

// A change to the settings, expressed as a transformation of the current value.
typealias SettingsUpdate = ((AppSettings) -> AppSettings) -> Unit

// The settings tab is a single level of sub-pages, so the current page is plain state
// instead of a navigation graph. The system back button returns to the main page, and
// leaving the tab resets it because this composable leaves the composition.
@Composable
fun SettingsHost(
    settings: AppSettings,
    isAdminActive: Boolean,
    hasCameraPermission: Boolean,
    notificationsEnabled: Boolean,
    eventCount: Int,
    appVersion: String,
    onUpdate: SettingsUpdate,
    onActivateAdmin: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestNotificationPermission: (() -> Unit)?,
    onOpenAppSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onOpenGallery: () -> Unit
) {
    var route by rememberSaveable { mutableStateOf(SettingsRoute.MAIN) }

    BackHandler(enabled = route != SettingsRoute.MAIN) {
        route = SettingsRoute.MAIN
    }

    val overview = protectionOverview(settings, isAdminActive, hasCameraPermission)
    val goBack = { route = SettingsRoute.MAIN }

    when (route) {
        SettingsRoute.MAIN -> SettingsMainScreen(
            overview = overview,
            onNavigate = { route = it }
        )

        SettingsRoute.GLOBAL_PROTECTION -> GlobalProtectionScreen(
            settings = settings,
            overview = overview,
            onUpdate = onUpdate,
            onBack = goBack
        )

        SettingsRoute.MONITORING -> MonitoringScreen(
            settings = settings,
            isAdminActive = isAdminActive,
            onUpdate = onUpdate,
            onActivateAdmin = onActivateAdmin,
            onBack = goBack
        )

        SettingsRoute.PHOTO_CAPTURE -> PhotoCaptureScreen(
            settings = settings,
            hasCameraPermission = hasCameraPermission,
            onUpdate = onUpdate,
            onRequestCameraPermission = onRequestCameraPermission,
            onOpenAppSettings = onOpenAppSettings,
            onBack = goBack
        )

        SettingsRoute.NOTIFICATIONS -> TechnicalNotificationsScreen(
            settings = settings,
            notificationsEnabled = notificationsEnabled,
            onUpdate = onUpdate,
            onRequestNotificationPermission = onRequestNotificationPermission,
            onOpenNotificationSettings = onOpenNotificationSettings,
            onBack = goBack
        )

        SettingsRoute.STORAGE -> LocalStorageScreen(
            eventCount = eventCount,
            onOpenGallery = onOpenGallery,
            onBack = goBack
        )

        SettingsRoute.ABOUT -> AboutScreen(
            versionName = appVersion,
            onBack = goBack
        )
    }
}
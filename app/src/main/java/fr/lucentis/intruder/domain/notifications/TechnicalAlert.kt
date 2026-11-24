package fr.lucentis.intruder.domain.notification

// The three kinds of technical problem the user can choose to be notified about. Each one
// matches an independent switch in the settings.
enum class TechnicalAlert {
    PROTECTION_INTERRUPTED,
    MISSING_PERMISSION,
    CAPTURE_PROBLEM
}

// Screens an alert can lead to. The domain only names the destination: the Android layer
// carries it in the notification and the UI maps it to a real screen.
enum class AlertScreen {
    EVENT_DETAIL,
    SETTINGS_GLOBAL_PROTECTION,
    SETTINGS_MONITORING,
    SETTINGS_PHOTO_CAPTURE,
    SETTINGS_STORAGE
}

// Where tapping a notification leads. eventId is only used by EVENT_DETAIL.
data class AlertTarget(
    val screen: AlertScreen,
    val eventId: Long? = null
)

// What the domain needs to report a problem. The Android implementation posts a system
// notification; tests record the alerts in memory.
interface TechnicalAlertNotifier {
    suspend fun notify(alert: TechnicalAlert, target: AlertTarget)
}

object NoOpTechnicalAlertNotifier : TechnicalAlertNotifier {
    override suspend fun notify(alert: TechnicalAlert, target: AlertTarget) = Unit
}
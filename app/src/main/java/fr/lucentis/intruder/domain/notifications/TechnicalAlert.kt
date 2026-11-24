package fr.lucentis.intruder.domain.notification

// The three kinds of technical problem the user can choose to be notified about. Each one
// matches an independent switch in the settings.
enum class TechnicalAlert {
    PROTECTION_INTERRUPTED,
    MISSING_PERMISSION,
    CAPTURE_PROBLEM
}

// What the domain needs to report a problem. The Android implementation posts a system
// notification; tests record the alerts in memory.
interface TechnicalAlertNotifier {
    suspend fun notify(alert: TechnicalAlert)
}

object NoOpTechnicalAlertNotifier : TechnicalAlertNotifier {
    override suspend fun notify(alert: TechnicalAlert) = Unit
}
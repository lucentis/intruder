package fr.lucentis.intruder.presentation.settings

import fr.lucentis.intruder.domain.model.AppSettings

enum class FeatureState { ACTIVE, WARNING, INACTIVE }

// Effective state of each protection feature, as the user experiences it. A feature the
// user turned on but whose Android permission is missing is a warning, not "active".
data class ProtectionOverview(
    val global: FeatureState,
    val successfulUnlocks: FeatureState,
    val failedAttempts: FeatureState,
    val capture: FeatureState
)

fun protectionOverview(
    settings: AppSettings,
    isAdminActive: Boolean,
    hasCameraPermission: Boolean
): ProtectionOverview {
    if (!settings.globalProtectionEnabled) {
        return ProtectionOverview(
            global = FeatureState.INACTIVE,
            successfulUnlocks = FeatureState.INACTIVE,
            failedAttempts = FeatureState.INACTIVE,
            capture = FeatureState.INACTIVE
        )
    }

    val successfulUnlocks = if (settings.monitorSuccessfulUnlocks) {
        FeatureState.ACTIVE
    } else {
        FeatureState.INACTIVE
    }

    val failedAttempts = when {
        !settings.monitorFailedAttempts -> FeatureState.INACTIVE
        !isAdminActive -> FeatureState.WARNING
        else -> FeatureState.ACTIVE
    }

    val capture = when {
        !settings.automaticCaptureEnabled -> FeatureState.INACTIVE
        !hasCameraPermission -> FeatureState.WARNING
        else -> FeatureState.ACTIVE
    }

    val hasWarning = listOf(successfulUnlocks, failedAttempts, capture)
        .any { it == FeatureState.WARNING }

    return ProtectionOverview(
        global = if (hasWarning) FeatureState.WARNING else FeatureState.ACTIVE,
        successfulUnlocks = successfulUnlocks,
        failedAttempts = failedAttempts,
        capture = capture
    )
}
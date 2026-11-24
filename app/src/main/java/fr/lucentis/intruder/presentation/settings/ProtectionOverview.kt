package fr.lucentis.intruder.presentation.settings

import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.presentation.common.FeatureState

// Effective state of each protection feature, as the user experiences it.
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
            global = FeatureState.DISABLED,
            successfulUnlocks = FeatureState.DISABLED,
            failedAttempts = FeatureState.DISABLED,
            capture = FeatureState.DISABLED
        )
    }

    // A feature the user turned off is disabled, never a warning: only an enabled feature
    // can be failing.
    val successfulUnlocks = if (settings.monitorSuccessfulUnlocks) {
        FeatureState.ACTIVE
    } else {
        FeatureState.DISABLED
    }

    val failedAttempts = when {
        !settings.monitorFailedAttempts -> FeatureState.DISABLED
        !isAdminActive -> FeatureState.WARNING
        else -> FeatureState.ACTIVE
    }

    val capture = when {
        !settings.automaticCaptureEnabled -> FeatureState.DISABLED
        !hasCameraPermission -> FeatureState.WARNING
        else -> FeatureState.ACTIVE
    }

    // The capture is not an event source: it only runs when a monitored event is detected.
    // With no source selected, nothing is watched, whatever the capture setting is.
    val hasEventSource = settings.monitorSuccessfulUnlocks || settings.monitorFailedAttempts

    val global = when {
        !hasEventSource -> FeatureState.INACTIVE
        failedAttempts == FeatureState.WARNING || capture == FeatureState.WARNING ->
            FeatureState.WARNING
        else -> FeatureState.ACTIVE
    }

    return ProtectionOverview(
        global = global,
        successfulUnlocks = successfulUnlocks,
        failedAttempts = failedAttempts,
        capture = capture
    )
}
package fr.lucentis.intruder.domain.model

enum class CameraFacing { FRONT, BACK }

enum class PhotoQuality { HIGH, STANDARD }

// Everything the user can configure. The default values are the ones the functional
// specification gives for the first launch. They express user choices, not the real
// state of the Android components (permissions, running services).
data class AppSettings(
    val globalProtectionEnabled: Boolean = false,
    val monitorSuccessfulUnlocks: Boolean = true,
    val monitorFailedAttempts: Boolean = true,
    val automaticCaptureEnabled: Boolean = true,
    val cameraFacing: CameraFacing = CameraFacing.FRONT,
    val photoQuality: PhotoQuality = PhotoQuality.HIGH,
    val notifyProtectionInterrupted: Boolean = true,
    val notifyMissingPermission: Boolean = true,
    val notifyCaptureProblem: Boolean = true
)
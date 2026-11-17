package fr.lucentis.intruder.domain.model

// Internal categories for capture errors. The camera layer converts Android error codes
// into these values, so the domain never depends on Camera2 classes. A missing reason
// (null) means the cause is unknown.
enum class CaptureFailureReason {
    PERMISSION_MISSING,
    CAMERA_UNAVAILABLE,
    CAMERA_BUSY,
    CAMERA_DISCONNECTED,
    CAMERA_ERROR,
    CAPTURE_FAILED,
    SAVE_FAILED
}
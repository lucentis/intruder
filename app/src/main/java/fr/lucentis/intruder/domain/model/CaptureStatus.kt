package fr.lucentis.intruder.domain.model

// Outcome of the capture attempt attached to an event. An event is always recorded,
// whatever the capture status is.
enum class CaptureStatus {
    SUCCESS,
    FAILED,
    SKIPPED
}
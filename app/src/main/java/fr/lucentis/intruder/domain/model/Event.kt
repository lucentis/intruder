package fr.lucentis.intruder.domain.model

// Domain model of an event, independent from Room. Timestamps are epoch milliseconds.
// An id of 0 means "not stored yet": the database assigns the real id on insert.
data class Event(
    val id: Long = 0,
    val type: EventType,
    val timestamp: Long,
    val photoPath: String?,
    val captureStatus: CaptureStatus,
    val failureReason: CaptureFailureReason?
)
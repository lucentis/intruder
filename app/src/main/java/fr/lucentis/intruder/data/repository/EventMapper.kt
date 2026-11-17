package fr.lucentis.intruder.data.repository

import fr.lucentis.intruder.data.database.EventEntity
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType

// Room entities stay inside the data layer. These functions translate in both directions:
// enums are stored by name, as text.
internal fun Event.toEntity() = EventEntity(
    id = id,
    type = type.name,
    timestamp = timestamp,
    photoPath = photoPath,
    captureStatus = captureStatus.name,
    failureReason = failureReason?.name
)

internal fun EventEntity.toDomain() = Event(
    id = id,
    type = EventType.valueOf(type),
    timestamp = timestamp,
    photoPath = photoPath,
    captureStatus = CaptureStatus.valueOf(captureStatus),
    // The reason is only diagnostic information: an unknown name becomes "no reason"
    // instead of making the whole event unreadable.
    failureReason = failureReason?.let { name ->
        CaptureFailureReason.entries.firstOrNull { it.name == name }
    }
)
package fr.lucentis.intruder.presentation.gallery

import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class GalleryFilter {
    ALL,
    SUCCESS,
    FAILED
}

data class GalleryDateGroup(
    val date: String,
    val events: List<Event>
)

// Inclusive range of calendar days. A single day is a period where start == end.
data class GalleryPeriod(
    val start: LocalDate,
    val end: LocalDate
)

fun List<Event>.filterByGalleryFilter(filter: GalleryFilter): List<Event> {
    return when (filter) {
        GalleryFilter.ALL -> this
        GalleryFilter.SUCCESS -> filter { it.type == EventType.UNLOCK_SUCCESS }
        GalleryFilter.FAILED -> filter { it.type == EventType.UNLOCK_FAILED }
    }
}

fun List<Event>.filterByPeriod(
    period: GalleryPeriod?,
    zone: ZoneId = ZoneId.systemDefault()
): List<Event> {
    if (period == null) return this

    return filter { event ->
        val day = Instant.ofEpochMilli(event.timestamp).atZone(zone).toLocalDate()
        !day.isBefore(period.start) && !day.isAfter(period.end)
    }
}

fun GalleryPeriod.label(locale: Locale = Locale.getDefault()): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", locale)

    return if (start == end) {
        start.format(formatter)
    } else {
        "${start.format(formatter)} – ${end.format(formatter)}"
    }
}

// Groups the events by calendar day in the device time zone, keeping the order of the list
// (newest first). LocalDate is a plain value that is cheap to create and to compare, unlike
// a Calendar. The zone, today's date and the locale are parameters so that the function can
// be tested with fixed values.
fun List<Event>.groupByDate(
    zone: ZoneId = ZoneId.systemDefault(),
    today: LocalDate = LocalDate.now(zone),
    locale: Locale = Locale.getDefault()
): List<GalleryDateGroup> {
    val yesterday = today.minusDays(1)
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)

    return groupBy { event ->
        Instant.ofEpochMilli(event.timestamp).atZone(zone).toLocalDate()
    }.map { (date, dayEvents) ->
        val label = when (date) {
            today -> "Aujourd'hui"
            yesterday -> "Hier"
            else -> date.format(formatter)
        }

        GalleryDateGroup(
            date = label,
            events = dayEvents
        )
    }
}

internal fun EventType.displayName(): String = when (this) {
    EventType.UNLOCK_SUCCESS -> "Déverrouillage réussi"
    EventType.UNLOCK_FAILED -> "Tentative échouée"
}

internal fun EventType.badgeLabel(): String = when (this) {
    EventType.UNLOCK_SUCCESS -> "Succès"
    EventType.UNLOCK_FAILED -> "Échec"
}

internal fun CaptureStatus.displayName(): String = when (this) {
    CaptureStatus.SUCCESS -> "Photo enregistrée"
    CaptureStatus.FAILED -> "Échec de la capture"
    CaptureStatus.SKIPPED -> "Capture ignorée"
}

internal fun CaptureFailureReason.displayName(): String = when (this) {
    CaptureFailureReason.PERMISSION_MISSING -> "Permission caméra manquante"
    CaptureFailureReason.CAMERA_UNAVAILABLE -> "Caméra indisponible"
    CaptureFailureReason.CAMERA_BUSY -> "Caméra occupée"
    CaptureFailureReason.CAMERA_DISCONNECTED -> "Caméra déconnectée"
    CaptureFailureReason.CAMERA_ERROR -> "Erreur de caméra"
    CaptureFailureReason.CAPTURE_FAILED -> "La prise de photo a échoué"
    CaptureFailureReason.SAVE_FAILED -> "L'enregistrement de la photo a échoué"
}

internal fun formatTime(
    timestamp: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String = DateTimeFormatter.ofPattern("HH:mm", locale)
    .format(Instant.ofEpochMilli(timestamp).atZone(zone))

internal fun formatDateTime(
    timestamp: Long,
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String = DateTimeFormatter.ofPattern("d MMMM yyyy 'à' HH:mm:ss", locale)
    .format(Instant.ofEpochMilli(timestamp).atZone(zone))
package fr.lucentis.intruder.presentation.gallery

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

fun List<Event>.filterByGalleryFilter(filter: GalleryFilter): List<Event> {
    return when (filter) {
        GalleryFilter.ALL -> this
        GalleryFilter.SUCCESS -> filter { it.type == EventType.UNLOCK_SUCCESS }
        GalleryFilter.FAILED -> filter { it.type == EventType.UNLOCK_FAILED }
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
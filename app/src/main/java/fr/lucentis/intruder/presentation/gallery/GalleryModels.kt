package fr.lucentis.intruder.presentation.gallery

import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

enum class GalleryFilter {
    ALL,
    SUCCESS,
    FAILED
}

enum class GalleryDisplayMode {
    GRID,
    LIST
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

fun List<Event>.groupByDate(): List<GalleryDateGroup> {
    val today = Calendar.getInstance()
    val yesterday = (today.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

    return groupBy { event ->
        Calendar.getInstance().apply {
            timeInMillis = event.timestamp
        }.get(Calendar.YEAR) to Calendar.getInstance().apply {
            timeInMillis = event.timestamp
        }.get(Calendar.DAY_OF_YEAR)
    }.map { (dateKey, events) ->
        val eventDate = Calendar.getInstance().apply {
            timeInMillis = events.first().timestamp
        }

        val label = when {
            eventDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                eventDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) ->
                "Aujourd'hui"

            eventDate.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                eventDate.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) ->
                "Hier"

            else -> dateFormat.format(Date(events.first().timestamp))
        }

        GalleryDateGroup(
            date = label,
            events = events
        )
    }
}
package fr.lucentis.intruder.presentation.gallery

import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType

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
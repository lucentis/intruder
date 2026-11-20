package fr.lucentis.intruder.presentation.gallery

import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

// Fixed zone, date and locale make the grouping deterministic on any machine.
class GalleryModelsTest {

    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2025, 3, 10)

    private fun eventAt(dateTime: LocalDateTime, id: Long) = Event(
        id = id,
        type = EventType.UNLOCK_SUCCESS,
        timestamp = dateTime.atZone(zone).toInstant().toEpochMilli(),
        photoPath = null,
        captureStatus = CaptureStatus.SKIPPED,
        failureReason = null
    )

    private fun group(events: List<Event>) =
        events.groupByDate(zone = zone, today = today, locale = Locale.ENGLISH)

    @Test
    fun groupsByDayKeepsTheListOrderAndLabelsTodayAndYesterday() {
        val events = listOf(
            eventAt(today.atTime(14, 0), id = 4),
            eventAt(today.atTime(9, 0), id = 3),
            eventAt(today.minusDays(1).atTime(22, 0), id = 2),
            eventAt(LocalDate.of(2025, 3, 5).atTime(8, 0), id = 1)
        )

        val groups = group(events)

        assertEquals(
            listOf("Aujourd'hui", "Hier", "5 March 2025"),
            groups.map { it.date }
        )
        assertEquals(listOf(listOf(4L, 3L), listOf(2L), listOf(1L)), groups.map { g -> g.events.map { it.id } })
    }

    @Test
    fun midnightSeparatesTwoDays() {
        val events = listOf(
            eventAt(today.atTime(0, 0), id = 2),
            eventAt(today.minusDays(1).atTime(23, 59), id = 1)
        )

        val groups = group(events)

        assertEquals(listOf("Aujourd'hui", "Hier"), groups.map { it.date })
    }

    @Test
    fun anEmptyListGivesNoGroup() {
        assertTrue(group(emptyList()).isEmpty())
    }
}
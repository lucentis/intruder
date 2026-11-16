package fr.lucentis.intruder

import fr.lucentis.intruder.data.database.EventEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

// Local unit test (src/test): runs on the JVM, on your machine or on CI, with no device.
// It cannot use real Android APIs (database, camera, services). Those need instrumented
// tests (src/androidTest) that run on a device, or manual checks on the phone.
class EventEntityTest {

    @Test
    fun newEventHasNoPersistedIdAndNoPhoto() {
        val event = EventEntity(
            type = "UNLOCK_FAILED",
            timestamp = 1L,
            photoPath = null,
            captureStatus = "SKIPPED",
            failureReason = null
        )

        assertEquals(0L, event.id)
        assertNull(event.photoPath)
    }
}
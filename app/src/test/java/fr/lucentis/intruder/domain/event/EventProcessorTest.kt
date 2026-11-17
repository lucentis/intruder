package fr.lucentis.intruder.domain.event

import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.domain.model.PhotoQuality
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

// These tests run on the JVM, with no device. The processor only depends on interfaces,
// so each dependency is replaced by a simple in-memory fake defined at the bottom.
// runBlocking runs a suspend function to completion from a plain (non-suspend) test.
class EventProcessorTest {

    private val settingsRepository = FakeSettingsRepository(
        AppSettings(globalProtectionEnabled = true)
    )
    private val photoCapturer = FakePhotoCapturer()
    private val eventRepository = FakeEventRepository()
    private val photoStorage = FakePhotoStorage()

    private val processor = EventProcessor(
        settingsRepository,
        photoCapturer,
        eventRepository,
        photoStorage
    )

    private val failedUnlock = EventCommand(EventType.UNLOCK_FAILED, detectedAt = 1_000L)
    private val successfulUnlock = EventCommand(EventType.UNLOCK_SUCCESS, detectedAt = 2_000L)

    @Test
    fun ignoresEventsWhenGlobalProtectionIsDisabled() = runBlocking<Unit> {
        settingsRepository.update { it.copy(globalProtectionEnabled = false) }

        val outcome = processor.process(failedUnlock)

        assertEquals(
            EventProcessingOutcome.Ignored(IgnoredReason.GLOBAL_PROTECTION_DISABLED),
            outcome
        )
        assertEquals(0, photoCapturer.calls)
        assertTrue(eventRepository.events.isEmpty())
    }

    @Test
    fun ignoresOnlyTheEventTypeWhoseMonitoringIsDisabled() = runBlocking<Unit> {
        settingsRepository.update { it.copy(monitorFailedAttempts = false) }

        assertEquals(
            EventProcessingOutcome.Ignored(IgnoredReason.MONITORING_DISABLED),
            processor.process(failedUnlock)
        )
        assertTrue(processor.process(successfulUnlock) is EventProcessingOutcome.Recorded)
        assertEquals(1, eventRepository.events.size)
    }

    @Test
    fun recordsTheEventWithItsPhotoWhenCaptureSucceeds() = runBlocking<Unit> {
        photoCapturer.result = CaptureResult.Success("/photos/a.jpg")

        val outcome = processor.process(successfulUnlock)

        val event = (outcome as EventProcessingOutcome.Recorded).event
        assertEquals(CaptureStatus.SUCCESS, event.captureStatus)
        assertEquals("/photos/a.jpg", event.photoPath)
        assertNull(event.failureReason)
        assertEquals(EventType.UNLOCK_SUCCESS, event.type)
        assertEquals(2_000L, event.timestamp)
        assertEquals(listOf(event), eventRepository.events)
    }

    @Test
    fun recordsTheEventWithoutPhotoWhenCaptureFails() = runBlocking<Unit> {
        photoCapturer.result = CaptureResult.Failure(CaptureFailureReason.CAMERA_BUSY)

        val outcome = processor.process(failedUnlock)

        val event = (outcome as EventProcessingOutcome.Recorded).event
        assertEquals(CaptureStatus.FAILED, event.captureStatus)
        assertNull(event.photoPath)
        assertEquals(CaptureFailureReason.CAMERA_BUSY, event.failureReason)
        assertEquals(1, eventRepository.events.size)
    }

    @Test
    fun keepsAnUnknownFailureReasonAsNull() = runBlocking<Unit> {
        photoCapturer.result = CaptureResult.Failure(null)

        val event = (processor.process(failedUnlock) as EventProcessingOutcome.Recorded).event

        assertEquals(CaptureStatus.FAILED, event.captureStatus)
        assertNull(event.failureReason)
    }

    @Test
    fun stillRecordsTheEventWhenTheCapturerThrowsUnexpectedly() = runBlocking<Unit> {
        photoCapturer.failure = IllegalStateException("camera driver crashed")

        val outcome = processor.process(failedUnlock)

        val event = (outcome as EventProcessingOutcome.Recorded).event
        assertEquals(CaptureStatus.FAILED, event.captureStatus)
        assertNull(event.photoPath)
        assertNull(event.failureReason)
        assertEquals(1, eventRepository.events.size)
    }

    @Test
    fun doesNotSwallowCoroutineCancellation() = runBlocking<Unit> {
        photoCapturer.failure = CancellationException("cancelled")

        try {
            processor.process(failedUnlock)
            fail("The cancellation should have propagated")
        } catch (exception: CancellationException) {
            // Expected: the work must stop, and nothing is recorded.
        }

        assertTrue(eventRepository.events.isEmpty())
    }

    @Test
    fun recordsASkippedEventAndNeverTouchesTheCameraWhenAutomaticCaptureIsDisabled() =
        runBlocking<Unit> {
            settingsRepository.update { it.copy(automaticCaptureEnabled = false) }

            val event =
                (processor.process(successfulUnlock) as EventProcessingOutcome.Recorded).event

            assertEquals(CaptureStatus.SKIPPED, event.captureStatus)
            assertNull(event.photoPath)
            assertEquals(0, photoCapturer.calls)
        }

    @Test
    fun passesTheCameraSettingsToTheCapturer() = runBlocking<Unit> {
        settingsRepository.update {
            it.copy(cameraFacing = CameraFacing.BACK, photoQuality = PhotoQuality.STANDARD)
        }

        processor.process(successfulUnlock)

        assertEquals(
            listOf(CameraFacing.BACK to PhotoQuality.STANDARD),
            photoCapturer.requests
        )
    }

    @Test
    fun reportsPersistenceFailureAndRemovesTheOrphanPhoto() = runBlocking<Unit> {
        photoCapturer.result = CaptureResult.Success("/photos/orphan.jpg")
        eventRepository.failOnInsert = true

        val outcome = processor.process(failedUnlock)

        val failed = outcome as EventProcessingOutcome.PersistenceFailed
        assertFalse(failed.orphanPhotoLeftBehind)
        assertEquals(listOf("/photos/orphan.jpg"), photoStorage.deleted)
        assertTrue(eventRepository.events.isEmpty())
    }

    @Test
    fun reportsAnOrphanPhotoThatCouldNotBeRemoved() = runBlocking<Unit> {
        photoCapturer.result = CaptureResult.Success("/photos/stuck.jpg")
        eventRepository.failOnInsert = true
        photoStorage.deleteSucceeds = false

        val failed = processor.process(failedUnlock) as EventProcessingOutcome.PersistenceFailed

        assertTrue(failed.orphanPhotoLeftBehind)
    }

    @Test
    fun producesOneEventPerCommandWithoutDeduplication() = runBlocking<Unit> {
        processor.process(failedUnlock)
        processor.process(failedUnlock)
        processor.process(successfulUnlock)

        assertEquals(3, eventRepository.events.size)
        assertEquals(3, eventRepository.events.map { it.id }.toSet().size)
        assertEquals(3, photoCapturer.calls)
    }
}

private class FakeSettingsRepository(initial: AppSettings) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<AppSettings> = state

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        state.value = transform(state.value)
    }
}

private class FakePhotoCapturer : PhotoCapturer {
    var result: CaptureResult = CaptureResult.Success("/photos/default.jpg")

    // When set, the next calls throw it instead of returning the result.
    var failure: Throwable? = null

    val requests = mutableListOf<Pair<CameraFacing, PhotoQuality>>()
    val calls: Int get() = requests.size

    override suspend fun capture(facing: CameraFacing, quality: PhotoQuality): CaptureResult {
        requests += facing to quality
        failure?.let { throw it }
        return result
    }
}

private class FakeEventRepository : EventRepository {
    val events = mutableListOf<Event>()
    var failOnInsert = false
    private var nextId = 1L

    override suspend fun insert(event: Event): Long {
        if (failOnInsert) throw EventPersistenceException(IllegalStateException("insert failed"))

        val id = nextId++
        events += event.copy(id = id)
        return id
    }

    override suspend fun getById(id: Long): Event? = events.firstOrNull { it.id == id }

    override fun observeCount(): Flow<Int> = flowOf(events.size)

    override fun observeLatest(): Flow<Event?> = flowOf(events.lastOrNull())

    override suspend fun deleteEvents(ids: List<Long>): DeleteResult {
        val before = events.size
        events.removeAll { it.id in ids }
        return DeleteResult(deletedEvents = before - events.size, photoDeletionFailures = 0)
    }
}

private class FakePhotoStorage : PhotoStorage {
    val deleted = mutableListOf<String>()
    var deleteSucceeds = true

    override fun delete(path: String): Boolean {
        deleted += path
        return deleteSucceeds
    }
}
package fr.lucentis.intruder.domain.event

import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.domain.model.PhotoQuality
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import fr.lucentis.intruder.domain.notification.TechnicalAlertNotifier
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventProcessorAlertsTest {

    private val settings = AlertTestSettings(AppSettings(globalProtectionEnabled = true))
    private val capturer = AlertTestCapturer()
    private val events = AlertTestEvents()
    private val alerts = RecordingNotifier()

    private val processor = EventProcessor(
        settingsRepository = settings,
        photoCapturer = capturer,
        eventRepository = events,
        photoStorage = AlertTestStorage(),
        alerts = alerts
    )

    private val failedUnlock = EventCommand(EventType.UNLOCK_FAILED, detectedAt = 1_000L)

    @Test
    fun noAlertWhenTheCaptureSucceeds() = runBlocking<Unit> {
        processor.process(failedUnlock)

        assertTrue(alerts.received.isEmpty())
    }

    @Test
    fun captureProblemWhenTheCaptureFails() = runBlocking<Unit> {
        capturer.result = CaptureResult.Failure(CaptureFailureReason.CAMERA_BUSY)

        processor.process(failedUnlock)

        assertEquals(listOf(TechnicalAlert.CAPTURE_PROBLEM), alerts.received)
    }

    @Test
    fun missingPermissionWhenTheCameraPermissionIsMissing() = runBlocking<Unit> {
        capturer.result = CaptureResult.Failure(CaptureFailureReason.PERMISSION_MISSING)

        processor.process(failedUnlock)

        assertEquals(listOf(TechnicalAlert.MISSING_PERMISSION), alerts.received)
    }

    @Test
    fun noAlertWhenTheCaptureIsDisabled() = runBlocking<Unit> {
        settings.update { it.copy(automaticCaptureEnabled = false) }

        processor.process(failedUnlock)

        assertTrue(alerts.received.isEmpty())
    }

    @Test
    fun captureProblemWhenTheEventCannotBeStored() = runBlocking<Unit> {
        events.failOnInsert = true

        processor.process(failedUnlock)

        assertEquals(listOf(TechnicalAlert.CAPTURE_PROBLEM), alerts.received)
    }

    @Test
    fun noAlertForAnIgnoredEvent() = runBlocking<Unit> {
        settings.update { it.copy(monitorFailedAttempts = false) }
        capturer.result = CaptureResult.Failure(CaptureFailureReason.CAMERA_BUSY)

        processor.process(failedUnlock)

        assertTrue(alerts.received.isEmpty())
    }

    @Test
    fun oneAlertPerOccurrence() = runBlocking<Unit> {
        capturer.result = CaptureResult.Failure(CaptureFailureReason.CAPTURE_FAILED)

        processor.process(failedUnlock)
        processor.process(failedUnlock)

        assertEquals(2, alerts.received.size)
    }

    @Test
    fun theEventIsStillRecordedWhenTheNotifierThrows() = runBlocking<Unit> {
        capturer.result = CaptureResult.Failure(CaptureFailureReason.CAMERA_BUSY)
        alerts.failure = IllegalStateException("notification service unavailable")

        val outcome = processor.process(failedUnlock)

        assertTrue(outcome is EventProcessingOutcome.Recorded)
        assertEquals(1, events.stored.size)
    }
}

private class RecordingNotifier : TechnicalAlertNotifier {
    val received = mutableListOf<TechnicalAlert>()
    var failure: Throwable? = null

    override suspend fun notify(alert: TechnicalAlert) {
        failure?.let { throw it }
        received += alert
    }
}

private class AlertTestSettings(initial: AppSettings) : SettingsRepository {
    private val state = MutableStateFlow(initial)

    override val settings: Flow<AppSettings> = state

    override suspend fun update(transform: (AppSettings) -> AppSettings) {
        state.value = transform(state.value)
    }
}

private class AlertTestCapturer : PhotoCapturer {
    var result: CaptureResult = CaptureResult.Success("/photos/default.jpg")

    override suspend fun capture(facing: CameraFacing, quality: PhotoQuality): CaptureResult =
        result
}

private class AlertTestEvents : EventRepository {
    val stored = mutableListOf<Event>()
    var failOnInsert = false

    override suspend fun insert(event: Event): Long {
        if (failOnInsert) throw EventPersistenceException(IllegalStateException("insert failed"))

        stored += event
        return stored.size.toLong()
    }

    override suspend fun getById(id: Long): Event? = null

    override fun observeCount(): Flow<Int> = flowOf(stored.size)

    override fun observeLatest(): Flow<Event?> = flowOf(null)

    override fun observeAll(): Flow<List<Event>> = flowOf(stored.toList())

    override suspend fun deleteEvents(ids: List<Long>): DeleteResult = DeleteResult(0, 0)
}

private class AlertTestStorage : PhotoStorage {
    override fun delete(path: String): Boolean = true
}
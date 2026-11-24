package fr.lucentis.intruder.domain.event

import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.domain.notification.AlertScreen
import fr.lucentis.intruder.domain.notification.AlertTarget
import fr.lucentis.intruder.domain.notification.NoOpTechnicalAlertNotifier
import fr.lucentis.intruder.domain.notification.TechnicalAlert
import fr.lucentis.intruder.domain.notification.TechnicalAlertNotifier
import fr.lucentis.intruder.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlin.coroutines.cancellation.CancellationException

// What the Android entry points (service, receivers) hand over: what happened and when.
data class EventCommand(
    val type: EventType,
    val detectedAt: Long
)

enum class IgnoredReason {
    GLOBAL_PROTECTION_DISABLED,
    MONITORING_DISABLED
}

sealed interface EventProcessingOutcome {
    data class Recorded(val event: Event) : EventProcessingOutcome
    data class Ignored(val reason: IgnoredReason) : EventProcessingOutcome
    data class PersistenceFailed(
        val cause: EventPersistenceException,
        // True when the photo taken for this event could not be removed afterwards.
        val orphanPhotoLeftBehind: Boolean
    ) : EventProcessingOutcome
}

// Temporary diagnostic information for the latest processed event.
// Unlike Event, this information is not persisted in Room.
data class EventProcessingDiagnostic(
    val command: EventCommand,
    val outcome: EventProcessingOutcome,
    val captureFailure: CaptureResult.Failure?
)

// Single place where the rules of the specification are applied, whatever the entry point:
// check the settings, try the capture at most once, then always record the event.
// It knows nothing about screens or Android components, so every dependency is replaceable
// by a fake in unit tests.
class EventProcessor(
    private val settingsRepository: SettingsRepository,
    private val photoCapturer: PhotoCapturer,
    private val eventRepository: EventRepository,
    private val photoStorage: PhotoStorage,
    private val alerts: TechnicalAlertNotifier = NoOpTechnicalAlertNotifier
) {

    // The latest diagnostic is kept in memory and exposed as a read-only Flow.
    // The UI can observe it without being able to modify it.
    private val _diagnostics = MutableStateFlow<EventProcessingDiagnostic?>(null)
    val diagnostics: StateFlow<EventProcessingDiagnostic?> = _diagnostics.asStateFlow()

    suspend fun process(command: EventCommand): EventProcessingOutcome {
        val settings = settingsRepository.settings.first()

        if (!settings.globalProtectionEnabled) {
            return publishDiagnostic(
                command = command,
                outcome = EventProcessingOutcome.Ignored(
                    IgnoredReason.GLOBAL_PROTECTION_DISABLED
                )
            )
        }

        if (!settings.isMonitoring(command.type)) {
            return publishDiagnostic(
                command = command,
                outcome = EventProcessingOutcome.Ignored(
                    IgnoredReason.MONITORING_DISABLED
                )
            )
        }

        // null means the capture was not attempted. No retry and no queue in V1.
        val capture: CaptureResult? =
            if (settings.automaticCaptureEnabled) attemptCapture(settings) else null

        val captureFailure = capture as? CaptureResult.Failure

        val event = Event(
            type = command.type,
            timestamp = command.detectedAt,
            photoPath = (capture as? CaptureResult.Success)?.filePath,
            captureStatus = when (capture) {
                null -> CaptureStatus.SKIPPED
                is CaptureResult.Success -> CaptureStatus.SUCCESS
                is CaptureResult.Failure -> CaptureStatus.FAILED
            },
            failureReason = captureFailure?.reason
        )

        val outcome = try {
            val id = eventRepository.insert(event)
            EventProcessingOutcome.Recorded(event.copy(id = id))
        } catch (exception: EventPersistenceException) {
            // The event was not stored: remove the photo so it is not left behind, and tell
            // the caller when that cleanup failed too.
            val orphanPhotoLeftBehind = event.photoPath?.let { path ->
                try {
                    !photoStorage.delete(path)
                } catch (cleanupException: Exception) {
                    // A cleanup failure must not hide the original persistence failure.
                    true
                }
            } ?: false

            EventProcessingOutcome.PersistenceFailed(exception, orphanPhotoLeftBehind)
        }

        alertOnProblem(outcome)

        return publishDiagnostic(command, outcome, captureFailure)
    }

    // One alert per occurrence: no grouping and no rate limit in V1. A problem tied to an
    // event leads to its detail screen. A missing permission is a lasting problem, so it
    // leads to the settings page that shows the warning.
    private suspend fun alertOnProblem(outcome: EventProcessingOutcome) {
        val report: Pair<TechnicalAlert, AlertTarget> = when (outcome) {
            is EventProcessingOutcome.Recorded -> when {
                outcome.event.captureStatus != CaptureStatus.FAILED -> null
                outcome.event.failureReason == CaptureFailureReason.PERMISSION_MISSING ->
                    TechnicalAlert.MISSING_PERMISSION to
                        AlertTarget(AlertScreen.SETTINGS_PHOTO_CAPTURE)
                else ->
                    TechnicalAlert.CAPTURE_PROBLEM to
                        AlertTarget(AlertScreen.EVENT_DETAIL, outcome.event.id)
            }

            is EventProcessingOutcome.PersistenceFailed ->
                TechnicalAlert.CAPTURE_PROBLEM to AlertTarget(AlertScreen.SETTINGS_STORAGE)

            is EventProcessingOutcome.Ignored -> null
        } ?: return

        try {
            alerts.notify(report.first, report.second)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            // Failing to notify must never hide the event that was just recorded.
        }
    }

    // Publishes the result for the diagnostic screen without changing the persisted Event.
    private fun publishDiagnostic(
        command: EventCommand,
        outcome: EventProcessingOutcome,
        captureFailure: CaptureResult.Failure? = null
    ): EventProcessingOutcome {
        _diagnostics.value = EventProcessingDiagnostic(
            command = command,
            outcome = outcome,
            captureFailure = captureFailure
        )

        return outcome
    }

    // The rule "every event is recorded" must hold even when the camera layer misbehaves,
    // so an unexpected exception is turned into a failed capture with an unknown reason.
    private suspend fun attemptCapture(settings: AppSettings): CaptureResult =
        try {
            photoCapturer.capture(settings.cameraFacing, settings.photoQuality)
        } catch (exception: CancellationException) {
            // Cancellation is how coroutines are stopped (for example when the service is
            // destroyed). It must always propagate: swallowing it would keep the work running.
            throw exception
        } catch (exception: Exception) {
            // Preserve unexpected camera errors for diagnostics without exposing
            // platform exceptions in the Event domain model.
            CaptureResult.Failure(
                reason = null,
                technicalDetails = "${exception.javaClass.simpleName}: ${exception.message}"
            )
        }
}

private fun AppSettings.isMonitoring(type: EventType): Boolean = when (type) {
    EventType.UNLOCK_SUCCESS -> monitorSuccessfulUnlocks
    EventType.UNLOCK_FAILED -> monitorFailedAttempts
}
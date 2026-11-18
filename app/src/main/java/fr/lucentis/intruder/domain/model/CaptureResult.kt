
package fr.lucentis.intruder.domain.model

// A sealed interface lists every possible subtype in one file, so a "when" over it is
// checked by the compiler. A capture returns an explicit result instead of throwing:
// a failure is an expected outcome here, not an exceptional one.
sealed interface CaptureResult {

    data class Success(
        val filePath: String
    ) : CaptureResult

    data class Failure(
        // A known reason when available. Null means the failure could not be classified.
        val reason: CaptureFailureReason?,

        // Technical information useful for debugging, without adding it to the Event model.
        val technicalDetails: String? = null,

        // Identifies the capture step where the failure occurred.
        val stage: CaptureStage? = null
    ) : CaptureResult
}

// Describes the capture pipeline stages to make failures easier to locate.
enum class CaptureStage {
    CAMERA_INITIALIZATION,
    CAMERA_OPENING,
    SESSION_CONFIGURATION,
    CAPTURE_REQUEST,
    IMAGE_ACQUISITION,
    PHOTO_ENCODING,
    FILE_CREATION,
    FILE_WRITING,
    CLEANUP
}

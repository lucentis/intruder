package fr.lucentis.intruder.domain.model

// A sealed interface lists every possible subtype in one file, so a "when" over it is
// checked by the compiler. A capture returns an explicit result instead of throwing:
// a failure is an expected outcome here, not an exceptional one.
sealed interface CaptureResult {
    data class Success(val filePath: String) : CaptureResult
    data class Failure(val reason: CaptureFailureReason?) : CaptureResult
}
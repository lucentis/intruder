
package fr.lucentis.intruder.platform.camera

import android.content.Context
import fr.lucentis.intruder.domain.event.PhotoCapturer
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.PhotoQuality
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class AndroidPhotoCapturer(
    private val context: Context,
    private val photosDirectory: File,
    private val captureTimeoutMs: Long = 8_000L
) : PhotoCapturer {

    override suspend fun capture(
        facing: CameraFacing,
        quality: PhotoQuality
    ): CaptureResult = suspendCancellableCoroutine { continuation ->

        // Camera2 uses callbacks, so suspendCancellableCoroutine adapts
        // this asynchronous API to our suspend-based domain interface.
        val cameraCapture = CameraCapture(
            context = context,
            photosDirectory = photosDirectory,
            facing = facing,
            quality = quality,
            onFinished = { result ->

                // A cancelled coroutine must not receive a late capture result.
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            },
            captureTimeoutMs = captureTimeoutMs
        )

        // If the caller is cancelled, request cancellation of the camera capture.
        continuation.invokeOnCancellation {
            cameraCapture.cancel()
        }

        cameraCapture.start()
    }
}

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
    private val photosDirectory: File
) : PhotoCapturer {

    override suspend fun capture(
        facing: CameraFacing,
        quality: PhotoQuality
    ): CaptureResult = suspendCancellableCoroutine { continuation ->

        val cameraCapture = CameraCapture(
            context = context,
            photosDirectory = photosDirectory,
            facing = facing,
            quality = quality,
            onFinished = { result ->
                if (continuation.isActive) {
                    continuation.resume(result)
                }
            }
        )

        continuation.invokeOnCancellation {
            cameraCapture.cancel()
        }

        cameraCapture.start()
    }
}

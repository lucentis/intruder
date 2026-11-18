
package fr.lucentis.intruder.platform.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureFailure
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.CaptureStage
import fr.lucentis.intruder.domain.model.PhotoQuality
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Performs a single photo capture using Android Camera2.
 *
 * This class only handles camera operations and file creation.
 * The caller is responsible for running it from the appropriate Android component.
 */
class CameraCapture(
    context: Context,
    private val photosDirectory: File,
    private val facing: CameraFacing,
    private val quality: PhotoQuality,
    private val onFinished: (CaptureResult) -> Unit
) {

    private val appContext = context.applicationContext

    private var cameraThread: HandlerThread? = null
    private var cameraHandler: Handler? = null

    private var cameraDevice: CameraDevice? = null
    private var imageReader: ImageReader? = null
    private var captureSession: CameraCaptureSession? = null

    private val finished = AtomicBoolean(false)

    @Volatile
    private var currentStage = CaptureStage.CAMERA_INITIALIZATION

    private var timeoutRunnable: Runnable? = null

    companion object {
        // Prevents a capture from remaining blocked indefinitely.
        private const val CAPTURE_TIMEOUT_MS = 15_000L
    }

    fun start() {
        if (finished.get()) return

        cameraThread = HandlerThread("IntruderCameraThread").apply {
            start()
        }

        cameraHandler = Handler(cameraThread!!.looper)

        // Camera2 callbacks and the timeout are handled on the same thread.
        timeoutRunnable = Runnable {
            fail(
                CaptureFailureReason.CAPTURE_FAILED,
                "Camera capture timed out after ${CAPTURE_TIMEOUT_MS}ms"
            )
        }

        cameraHandler?.postDelayed(
            timeoutRunnable!!,
            CAPTURE_TIMEOUT_MS
        )

        try {
            currentStage = CaptureStage.CAMERA_INITIALIZATION

            val cameraManager =
                appContext.getSystemService(CameraManager::class.java)

            val requestedFacing = when (facing) {
                CameraFacing.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
                CameraFacing.BACK -> CameraCharacteristics.LENS_FACING_BACK
            }

            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == requestedFacing
            }

            if (cameraId == null) {
                fail(
                    CaptureFailureReason.CAMERA_UNAVAILABLE,
                    "No camera found for the requested facing"
                )
                return
            }

            val characteristics =
                cameraManager.getCameraCharacteristics(cameraId)

            val map = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )

            val sizes = map?.getOutputSizes(ImageFormat.JPEG)

            val size = selectImageSize(sizes?.toList())

            if (size == null) {
                fail(
                    CaptureFailureReason.CAMERA_UNAVAILABLE,
                    "No compatible JPEG output size found"
                )
                return
            }

            imageReader = ImageReader.newInstance(
                size.width,
                size.height,
                ImageFormat.JPEG,
                2
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                if (finished.get()) return@setOnImageAvailableListener

                currentStage = CaptureStage.IMAGE_ACQUISITION

                var image: android.media.Image? = null

                try {
                    image = reader.acquireNextImage()

                    if (image == null) {
                        fail(
                            CaptureFailureReason.CAPTURE_FAILED,
                            "ImageReader returned no image"
                        )
                        return@setOnImageAvailableListener
                    }

                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    savePhoto(bytes)

                } catch (exception: Exception) {
                    fail(
                        CaptureFailureReason.CAPTURE_FAILED,
                        "${exception.javaClass.simpleName}: ${exception.message}"
                    )
                } finally {
                    image?.close()
                }

            }, cameraHandler)

            currentStage = CaptureStage.CAMERA_OPENING

            cameraManager.openCamera(
                cameraId,
                object : CameraDevice.StateCallback() {

                    override fun onOpened(camera: CameraDevice) {
                        if (finished.get()) {
                            camera.close()
                            return
                        }

                        cameraDevice = camera
                        createCaptureSession(camera)
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                        fail(
                            CaptureFailureReason.CAMERA_DISCONNECTED,
                            "Camera disconnected"
                        )
                    }

                    override fun onError(
                        camera: CameraDevice,
                        error: Int
                    ) {
                        camera.close()

                        val reason = when (error) {
                            CameraDevice.StateCallback.ERROR_CAMERA_IN_USE,
                            CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE ->
                                CaptureFailureReason.CAMERA_BUSY

                            else -> CaptureFailureReason.CAMERA_ERROR
                        }

                        fail(
                            reason,
                            "Camera device error code: $error"
                        )
                    }
                },
                cameraHandler
            )

        } catch (exception: SecurityException) {
            fail(
                CaptureFailureReason.PERMISSION_MISSING,
                "${exception.javaClass.simpleName}: ${exception.message}"
            )
        } catch (exception: Exception) {
            fail(
                CaptureFailureReason.CAMERA_ERROR,
                "${exception.javaClass.simpleName}: ${exception.message}"
            )
        }
    }

    
    fun cancel() {
        // Camera2 resources must be released on the camera thread.
        val handler = cameraHandler

        val cancellation = {
            fail(
                CaptureFailureReason.CAPTURE_FAILED,
                "Camera capture was cancelled"
            )
        }

        // If the camera thread is not available yet, no camera operation
        // has started, or its looper is no longer accepting tasks.
        if (handler == null || !handler.post(cancellation)) {
            cancellation()
        }
    }


    private fun selectImageSize(sizes: List<Size>?): Size? {
        if (sizes.isNullOrEmpty()) return null

        val maxDimension = when (quality) {
            PhotoQuality.HIGH -> 1920
            PhotoQuality.STANDARD -> 1280
        }

        return sizes
            .filter { it.width <= maxDimension && it.height <= maxDimension }
            .maxByOrNull { it.width.toLong() * it.height }
            ?: sizes.maxByOrNull {
                it.width.toLong() * it.height
            }
    }

    private fun createCaptureSession(camera: CameraDevice) {
        if (finished.get()) return

        currentStage = CaptureStage.SESSION_CONFIGURATION

        val reader = imageReader ?: run {
            fail(
                CaptureFailureReason.CAMERA_ERROR,
                "ImageReader is not available"
            )
            return
        }

        try {
            camera.createCaptureSession(
                listOf(reader.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(session: CameraCaptureSession) {
                        if (finished.get()) {
                            session.close()
                            return
                        }

                        captureSession = session

                        try {
                            currentStage = CaptureStage.CAPTURE_REQUEST

                            val request = camera.createCaptureRequest(
                                CameraDevice.TEMPLATE_STILL_CAPTURE
                            ).apply {
                                addTarget(reader.surface)
                                set(
                                    CaptureRequest.CONTROL_MODE,
                                    CaptureRequest.CONTROL_MODE_AUTO
                                )
                            }

                            session.capture(
                                request.build(),
                                object : CameraCaptureSession.CaptureCallback() {

                                    override fun onCaptureFailed(
                                        session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        failure: CaptureFailure
                                    ) {
                                        fail(
                                            CaptureFailureReason.CAPTURE_FAILED,
                                            "Camera capture failed: ${failure.reason}"
                                        )
                                    }

                                    override fun onCaptureSequenceAborted(
                                        session: CameraCaptureSession,
                                        sequenceId: Int
                                    ) {
                                        fail(
                                            CaptureFailureReason.CAPTURE_FAILED,
                                            "Camera capture sequence was aborted"
                                        )
                                    }
                                },
                                cameraHandler
                            )

                        } catch (exception: Exception) {
                            fail(
                                CaptureFailureReason.CAPTURE_FAILED,
                                "${exception.javaClass.simpleName}: ${exception.message}"
                            )
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        fail(
                            CaptureFailureReason.CAMERA_ERROR,
                            "Camera capture session configuration failed"
                        )
                    }
                },
                cameraHandler
            )

        } catch (exception: Exception) {
            fail(
                CaptureFailureReason.CAMERA_ERROR,
                "${exception.javaClass.simpleName}: ${exception.message}"
            )
        }
    }

    private fun savePhoto(bytes: ByteArray) {
        var temporaryFile: File? = null

        try {
            currentStage = CaptureStage.FILE_CREATION

            if (!photosDirectory.exists() && !photosDirectory.mkdirs()) {
                throw IOException("Unable to create photo directory")
            }

            val id = UUID.randomUUID()

            temporaryFile = File(
                photosDirectory,
                "capture_$id.tmp"
            )

            val finalFile = File(
                photosDirectory,
                "capture_$id.jpg"
            )

            currentStage = CaptureStage.FILE_WRITING

            // The temporary file is renamed only after the write succeeds.
            FileOutputStream(temporaryFile).use { output ->
                output.write(bytes)
                output.flush()
            }

            if (!temporaryFile.renameTo(finalFile)) {
                throw IOException("Unable to finalize captured photo")
            }

            temporaryFile = null

            finish(
                CaptureResult.Success(finalFile.absolutePath)
            )

        } catch (exception: Exception) {
            fail(
                CaptureFailureReason.SAVE_FAILED,
                "${exception.javaClass.simpleName}: ${exception.message}"
            )
        } finally {
            // A failed or interrupted write must not leave an incomplete photo.
            temporaryFile?.delete()
        }
    }

    private fun fail(
        reason: CaptureFailureReason,
        details: String?
    ) {
        finish(
            CaptureResult.Failure(
                reason = reason,
                technicalDetails = details,
                stage = currentStage
            )
        )
    }

    private fun finish(result: CaptureResult) {
        if (!finished.compareAndSet(false, true)) return

        currentStage = CaptureStage.CLEANUP

        timeoutRunnable?.let { runnable ->
            cameraHandler?.removeCallbacks(runnable)
        }
        timeoutRunnable = null

        // Each resource is closed independently so one cleanup failure
        // does not prevent the remaining resources from being released.
        runCatching { captureSession?.close() }
        captureSession = null

        runCatching { cameraDevice?.close() }
        cameraDevice = null

        runCatching { imageReader?.close() }
        imageReader = null

        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null

        onFinished(result)
    }
}

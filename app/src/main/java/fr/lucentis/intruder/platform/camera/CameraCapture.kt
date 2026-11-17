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

    fun start() {
        if (finished.get()) return

        cameraThread = HandlerThread("IntruderCameraThread").apply {
            start()
        }

        cameraHandler = Handler(cameraThread!!.looper)

        try {
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
                finish(CaptureResult.Failure(CaptureFailureReason.CAMERA_UNAVAILABLE))
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
                finish(CaptureResult.Failure(CaptureFailureReason.CAMERA_UNAVAILABLE))
                return
            }

            imageReader = ImageReader.newInstance(
                size.width,
                size.height,
                ImageFormat.JPEG,
                2
            )

            imageReader?.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()

                if (image == null) {
                    finish(CaptureResult.Failure(CaptureFailureReason.CAPTURE_FAILED))
                    return@setOnImageAvailableListener
                }

                var result: CaptureResult? = null

                try {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)

                    if (!photosDirectory.exists() && !photosDirectory.mkdirs()) {
                        throw IOException("Unable to create photo directory")
                    }

                    val file = File(
                        photosDirectory,
                        "capture_${UUID.randomUUID()}.jpg"
                    )

                    FileOutputStream(file).use { output ->
                        output.write(bytes)
                    }

                    result = CaptureResult.Success(file.absolutePath)

                } catch (exception: Exception) {
                    result = CaptureResult.Failure(CaptureFailureReason.SAVE_FAILED)
                } finally {
                    image.close()
                }

                result?.let(::finish)

            }, cameraHandler)

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
                        finish(
                            CaptureResult.Failure(
                                CaptureFailureReason.CAMERA_DISCONNECTED
                            )
                        )
                    }

                    override fun onError(
                        camera: CameraDevice,
                        error: Int
                    ) {
                        camera.close()
                        finish(
                            CaptureResult.Failure(
                                CaptureFailureReason.CAMERA_ERROR
                            )
                        )
                    }
                },
                cameraHandler
            )

        } catch (exception: SecurityException) {
            finish(
                CaptureResult.Failure(CaptureFailureReason.PERMISSION_MISSING)
            )
        } catch (exception: Exception) {
            finish(
                CaptureResult.Failure(CaptureFailureReason.CAMERA_ERROR)
            )
        }
    }

    fun cancel() {
        finish(
            CaptureResult.Failure(CaptureFailureReason.CAPTURE_FAILED)
        )
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

        val reader = imageReader ?: run {
            finish(
                CaptureResult.Failure(CaptureFailureReason.CAMERA_ERROR)
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
                                        finish(
                                            CaptureResult.Failure(
                                                CaptureFailureReason.CAPTURE_FAILED
                                            )
                                        )
                                    }
                                },
                                cameraHandler
                            )

                        } catch (exception: Exception) {
                            finish(
                                CaptureResult.Failure(
                                    CaptureFailureReason.CAPTURE_FAILED
                                )
                            )
                        }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        finish(
                            CaptureResult.Failure(CaptureFailureReason.CAMERA_ERROR)
                        )
                    }
                },
                cameraHandler
            )

        } catch (exception: Exception) {
            finish(
                CaptureResult.Failure(CaptureFailureReason.CAMERA_ERROR)
            )
        }
    }

    private fun finish(result: CaptureResult) {
        if (!finished.compareAndSet(false, true)) return

        captureSession?.close()
        captureSession = null

        cameraDevice?.close()
        cameraDevice = null

        imageReader?.close()
        imageReader = null

        cameraThread?.quitSafely()
        cameraThread = null
        cameraHandler = null

        onFinished(result)
    }
}

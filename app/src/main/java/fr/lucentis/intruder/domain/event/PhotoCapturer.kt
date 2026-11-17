package fr.lucentis.intruder.domain.event

import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.CaptureResult
import fr.lucentis.intruder.domain.model.PhotoQuality

// What the domain needs from the camera. The Camera2 implementation arrives with the
// platform layer; tests use a fake.
//
// Contract: an expected failure (missing permission, busy camera, ...) is returned as
// CaptureResult.Failure and never thrown. EventProcessor still guards against unexpected
// exceptions, but an implementation must not rely on that safety net.
interface PhotoCapturer {
    suspend fun capture(facing: CameraFacing, quality: PhotoQuality): CaptureResult
}
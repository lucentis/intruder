package fr.lucentis.intruder.presentation.settings

import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.presentation.common.FeatureState
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtectionOverviewTest {

    private val enabled = AppSettings(globalProtectionEnabled = true)

    @Test
    fun everythingIsDisabledWhenTheGlobalProtectionIsOff() {
        val overview = protectionOverview(
            settings = AppSettings(globalProtectionEnabled = false),
            isAdminActive = true,
            hasCameraPermission = true
        )

        assertEquals(FeatureState.DISABLED, overview.global)
        assertEquals(FeatureState.DISABLED, overview.successfulUnlocks)
        assertEquals(FeatureState.DISABLED, overview.failedAttempts)
        assertEquals(FeatureState.DISABLED, overview.capture)
    }

    @Test
    fun everythingIsActiveWhenPermissionsAreGranted() {
        val overview = protectionOverview(enabled, isAdminActive = true, hasCameraPermission = true)

        assertEquals(FeatureState.ACTIVE, overview.global)
        assertEquals(FeatureState.ACTIVE, overview.successfulUnlocks)
        assertEquals(FeatureState.ACTIVE, overview.failedAttempts)
        assertEquals(FeatureState.ACTIVE, overview.capture)
    }

    @Test
    fun globalIsInactiveWhenNoEventSourceIsSelected() {
        val settings = enabled.copy(
            monitorSuccessfulUnlocks = false,
            monitorFailedAttempts = false,
            automaticCaptureEnabled = true
        )

        val overview = protectionOverview(settings, isAdminActive = true, hasCameraPermission = true)

        assertEquals(FeatureState.INACTIVE, overview.global)
    }

    @Test
    fun captureAloneIsNotAnEventSource() {
        val settings = enabled.copy(
            monitorSuccessfulUnlocks = false,
            monitorFailedAttempts = false,
            automaticCaptureEnabled = true
        )

        val overview = protectionOverview(settings, isAdminActive = false, hasCameraPermission = false)

        assertEquals(FeatureState.INACTIVE, overview.global)
        assertEquals(FeatureState.WARNING, overview.capture)
    }

    @Test
    fun missingAdminAuthorizationWarnsOnFailedAttempts() {
        val overview = protectionOverview(enabled, isAdminActive = false, hasCameraPermission = true)

        assertEquals(FeatureState.WARNING, overview.failedAttempts)
        assertEquals(FeatureState.ACTIVE, overview.capture)
        assertEquals(FeatureState.WARNING, overview.global)
    }

    @Test
    fun missingCameraPermissionWarnsOnCapture() {
        val overview = protectionOverview(enabled, isAdminActive = true, hasCameraPermission = false)

        assertEquals(FeatureState.WARNING, overview.capture)
        assertEquals(FeatureState.ACTIVE, overview.failedAttempts)
        assertEquals(FeatureState.WARNING, overview.global)
    }

    @Test
    fun aDisabledFeatureNeverWarnsAboutItsPermission() {
        val settings = enabled.copy(
            monitorFailedAttempts = false,
            automaticCaptureEnabled = false
        )

        val overview = protectionOverview(settings, isAdminActive = false, hasCameraPermission = false)

        assertEquals(FeatureState.DISABLED, overview.failedAttempts)
        assertEquals(FeatureState.DISABLED, overview.capture)
        assertEquals(FeatureState.ACTIVE, overview.global)
    }
}
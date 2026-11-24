package fr.lucentis.intruder.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.runtime.Composable
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.PhotoQuality

@Composable
fun PhotoCaptureScreen(
    settings: AppSettings,
    hasCameraPermission: Boolean,
    onUpdate: SettingsUpdate,
    onRequestCameraPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onBack: () -> Unit
) {
    SettingsSubScreen(title = "Capture photo", onBack = onBack) {
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Capture automatique",
                description = "Activer ou désactiver les captures photo.",
                checked = settings.automaticCaptureEnabled,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(automaticCaptureEnabled = checked) }
                }
            )

            PermissionNotice(
                granted = hasCameraPermission,
                grantedLabel = "Autorisation caméra : accordée",
                warning = "L'autorisation caméra est nécessaire pour prendre les photos."
            ) {
                WarningAction(label = "Autoriser", onClick = onRequestCameraPermission)
                WarningAction(
                    label = "Paramètres Android",
                    onClick = onOpenAppSettings
                )
            }
        }

        // Display only in V1: the front camera and the high quality are fixed values.
        SettingsCard(title = "Paramètres de capture") {
            SettingsValueRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Caméra",
                description = "Caméra utilisée pour les captures.",
                value = when (settings.cameraFacing) {
                    CameraFacing.FRONT -> "Avant"
                    CameraFacing.BACK -> "Arrière"
                }
            )

            SettingsDivider()

            SettingsValueRow(
                icon = Icons.Outlined.Image,
                title = "Qualité photo",
                description = "Niveau de qualité des photos capturées.",
                value = when (settings.photoQuality) {
                    PhotoQuality.HIGH -> "Élevée"
                    PhotoQuality.STANDARD -> "Standard"
                }
            )
        }

        SettingsInfoCard(
            text = "La capture photo est indépendante des réglages de surveillance."
        )
    }
}
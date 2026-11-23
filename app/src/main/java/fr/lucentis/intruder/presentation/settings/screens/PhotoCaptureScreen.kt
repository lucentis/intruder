package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CameraFacing
import fr.lucentis.intruder.domain.model.PhotoQuality
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsPageHeader
import fr.lucentis.intruder.presentation.settings.components.SettingsStatusBadge
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun PhotoCaptureScreen(
    settings: AppSettings,
    hasCameraPermission: Boolean,
    onBack: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onToggleAutomaticCapture: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "Capture photo",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                CaptureSwitchRow(
                    title = "Capture automatique",
                    description = "Prendre une photo lors d'un événement surveillé",
                    checked = settings.automaticCaptureEnabled,
                    onCheckedChange = onToggleAutomaticCapture
                )
            }
        }

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Paramètres de capture",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                ReadOnlySettingRow(
                    title = "Caméra",
                    value = when (settings.cameraFacing) {
                        CameraFacing.FRONT -> "Avant"
                        CameraFacing.BACK -> "Arrière"
                    }
                )

                HorizontalDivider(color = IntruderColors.Border)

                ReadOnlySettingRow(
                    title = "Qualité photo",
                    value = when (settings.photoQuality) {
                        PhotoQuality.HIGH -> "Élevée"
                        PhotoQuality.STANDARD -> "Standard"
                    }
                )

                Text(
                    text = "Ces paramètres seront personnalisables dans une prochaine version.",
                    style = IntruderTypography.Caption,
                    color = IntruderColors.TextSecondary
                )
            }
        }

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Autorisation caméra",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Intruder a besoin de l'accès à la caméra pour prendre les photos de surveillance.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                SettingsStatusBadge(
                    label = if (hasCameraPermission) {
                        "Autorisation accordée"
                    } else {
                        "Autorisation requise"
                    },
                    positive = hasCameraPermission
                )

                if (!hasCameraPermission) {
                    androidx.compose.material3.Button(
                        onClick = onRequestCameraPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Autoriser l'accès à la caméra",
                            style = IntruderTypography.BodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = IntruderTypography.BodyMedium,
                color = IntruderColors.TextPrimary
            )

            Text(
                text = description,
                style = IntruderTypography.Caption,
                color = IntruderColors.TextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = IntruderColors.OnPrimary,
                checkedTrackColor = IntruderColors.Primary,
                checkedBorderColor = IntruderColors.Primary,
                uncheckedThumbColor = IntruderColors.TextTertiary,
                uncheckedTrackColor = IntruderColors.SurfaceVariant,
                uncheckedBorderColor = IntruderColors.Border
            )
        )
    }
}

@Composable
private fun ReadOnlySettingRow(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = IntruderTypography.Body,
            color = IntruderColors.TextSecondary
        )

        Text(
            text = value,
            style = IntruderTypography.BodyMedium,
            color = IntruderColors.TextPrimary
        )
    }
}
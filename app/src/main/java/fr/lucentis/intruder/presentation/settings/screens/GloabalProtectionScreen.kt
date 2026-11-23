
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsPageHeader
import fr.lucentis.intruder.presentation.settings.components.SettingsStatusBadge
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GlobalProtectionScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onToggleGlobalProtection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "Protection globale",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            text = "Protection globale",
                            style = IntruderTypography.TitleSmall,
                            color = IntruderColors.TextPrimary
                        )

                        Text(
                            text = "Activer ou désactiver la protection",
                            style = IntruderTypography.Body,
                            color = IntruderColors.TextSecondary
                        )
                    }

                    Switch(
                        checked = settings.globalProtectionEnabled,
                        onCheckedChange = { onToggleGlobalProtection() },
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

                SettingsStatusBadge(
                    label = if (settings.globalProtectionEnabled) {
                        "Protection activée"
                    } else {
                        "Protection désactivée"
                    },
                    positive = settings.globalProtectionEnabled
                )
            }
        }

        SettingsCategoryCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Fonctionnement",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                ProtectionStatusRow(
                    title = "Déverrouillages réussis",
                    enabled = settings.globalProtectionEnabled &&
                        settings.monitorSuccessfulUnlocks
                )

                HorizontalDivider(color = IntruderColors.Border)

                ProtectionStatusRow(
                    title = "Codes PIN échoués",
                    enabled = settings.globalProtectionEnabled &&
                        settings.monitorFailedAttempts
                )

                HorizontalDivider(color = IntruderColors.Border)

                ProtectionStatusRow(
                    title = "Capture photo",
                    enabled = settings.globalProtectionEnabled &&
                        settings.automaticCaptureEnabled
                )
            }
        }

        Surface(
            color = IntruderColors.SurfaceVariant,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Lorsque la protection globale est désactivée, la surveillance et les captures automatiques sont interrompues. Les réglages individuels sont conservés.",
                modifier = Modifier.padding(16.dp),
                style = IntruderTypography.Body,
                color = IntruderColors.TextSecondary
            )
        }
    }
}

@Composable
private fun ProtectionStatusRow(
    title: String,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = IntruderTypography.Body,
            color = IntruderColors.TextPrimary
        )

        SettingsStatusBadge(
            label = if (enabled) "Active" else "Inactive",
            positive = enabled
        )
    }
}

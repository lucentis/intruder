
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun MonitoringScreen(
    settings: AppSettings,
    isAdminActive: Boolean,
    onBack: () -> Unit,
    onActivateAdmin: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "Surveillance",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MonitoringSwitchRow(
                    title = "Déverrouillages réussis",
                    description = "Surveiller les déverrouillages normaux",
                    checked = settings.monitorSuccessfulUnlocks,
                    onCheckedChange = onToggleSuccessfulUnlocks
                )

                HorizontalDivider(color = IntruderColors.Border)

                MonitoringSwitchRow(
                    title = "Codes PIN échoués",
                    description = "Détecter les tentatives de déverrouillage échouées",
                    checked = settings.monitorFailedAttempts,
                    enabled = isAdminActive,
                    onCheckedChange = onToggleFailedAttempts
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
                    text = "Autorisation administrateur",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Cette autorisation Android est nécessaire pour détecter les codes PIN échoués.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                SettingsStatusBadge(
                    label = if (isAdminActive) {
                        "Autorisation accordée"
                    } else {
                        "Autorisation requise"
                    },
                    positive = isAdminActive
                )

                if (!isAdminActive) {
                    Button(
                        onClick = onActivateAdmin,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IntruderColors.Primary,
                            contentColor = IntruderColors.OnPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Activer l'autorisation",
                            style = IntruderTypography.BodyMedium
                        )
                    }
                } else {
                    Text(
                        text = "La détection des codes PIN échoués peut être utilisée.",
                        style = IntruderTypography.Caption,
                        color = IntruderColors.Success
                    )
                }
            }
        }
    }
}

@Composable
private fun MonitoringSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean = true,
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
                color = if (enabled) {
                    IntruderColors.TextPrimary
                } else {
                    IntruderColors.TextTertiary
                }
            )

            Text(
                text = description,
                style = IntruderTypography.Caption,
                color = IntruderColors.TextSecondary
            )
        }

        Switch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

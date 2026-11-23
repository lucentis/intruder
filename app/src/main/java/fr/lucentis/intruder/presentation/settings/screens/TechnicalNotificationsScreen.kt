
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
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsPageHeader
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun TechnicalNotificationsScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onToggleProtectionInterrupted: () -> Unit,
    onToggleMissingPermission: () -> Unit,
    onToggleCaptureProblem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "Notifications techniques",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TechnicalNotificationRow(
                    title = "Protection interrompue",
                    description = "Signaler lorsque la protection ne fonctionne plus.",
                    checked = settings.notifyProtectionInterrupted,
                    onCheckedChange = onToggleProtectionInterrupted
                )

                HorizontalDivider(color = IntruderColors.Border)

                TechnicalNotificationRow(
                    title = "Autorisation manquante",
                    description = "Signaler lorsqu'une autorisation nécessaire n'est plus disponible.",
                    checked = settings.notifyMissingPermission,
                    onCheckedChange = onToggleMissingPermission
                )

                HorizontalDivider(color = IntruderColors.Border)

                TechnicalNotificationRow(
                    title = "Problème de capture",
                    description = "Signaler lorsqu'une photo n'a pas pu être prise ou enregistrée.",
                    checked = settings.notifyCaptureProblem,
                    onCheckedChange = onToggleCaptureProblem
                )
            }
        }

        SettingsCategoryCard {
            Text(
                text = "Désactiver une notification masque uniquement son affichage. La surveillance, les captures et la détection des problèmes continuent de fonctionner.",
                modifier = Modifier.padding(16.dp),
                style = IntruderTypography.Body,
                color = IntruderColors.TextSecondary
            )
        }
    }
}

@Composable
private fun TechnicalNotificationRow(
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

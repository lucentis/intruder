package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.presentation.common.FeatureState
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GlobalProtectionScreen(
    settings: AppSettings,
    overview: ProtectionOverview,
    onUpdate: SettingsUpdate,
    onBack: () -> Unit
) {
    SettingsSubScreen(title = "Protection globale", onBack = onBack) {
        SettingsCard {
            SettingsSwitchRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "Protection globale",
                description = "Activer ou désactiver la protection",
                checked = settings.globalProtectionEnabled,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(globalProtectionEnabled = checked) }
                },
                iconContainer = IntruderColors.SuccessContainer,
                iconTint = IntruderColors.Success
            )

            Row(
                modifier = Modifier.padding(start = 52.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = overview.global.badgeText(feminine = true),
                    kind = overview.global.badgeKind()
                )

                if (overview.global == FeatureState.INACTIVE) {
                    Text(
                        text = "Aucune surveillance n'est activée.",
                        style = IntruderTypography.Label,
                        color = IntruderColors.TextSecondary
                    )
                }
            }
        }

        SettingsCard(title = "État de la protection") {
            StatusRow(
                label = "Déverrouillages réussis",
                text = overview.successfulUnlocks.badgeText(feminine = false),
                kind = overview.successfulUnlocks.badgeKind()
            )

            SettingsDivider()

            StatusRow(
                label = "Codes PIN échoués",
                text = overview.failedAttempts.badgeText(feminine = false),
                kind = overview.failedAttempts.badgeKind()
            )

            SettingsDivider()

            StatusRow(
                label = "Capture photo",
                text = overview.capture.badgeText(feminine = true),
                kind = overview.capture.badgeKind()
            )
        }

        SettingsInfoCard(
            text = "La désactivation de la protection globale interrompt la " +
                "surveillance et les captures automatiques."
        )
    }
}

@Composable
private fun StatusRow(
    label: String,
    text: String,
    kind: BadgeKind
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = IntruderTypography.Body,
            color = IntruderColors.TextSecondary
        )

        StatusBadge(text = text, kind = kind)
    }
}
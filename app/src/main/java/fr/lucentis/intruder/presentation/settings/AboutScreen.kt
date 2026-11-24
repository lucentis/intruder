package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun AboutScreen(
    versionName: String,
    onBack: () -> Unit
) {
    SettingsSubScreen(title = "À propos", onBack = onBack) {
        SettingsCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(IntruderColors.SuccessContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = IntruderColors.Success
                    )
                }

                Text(
                    text = "Intruder",
                    style = IntruderTypography.Headline,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Protection et surveillance du téléphone",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                StatusBadge(
                    text = "Version $versionName",
                    kind = BadgeKind.POSITIVE
                )
            }
        }

        SettingsCard(title = "Informations") {
            InfoRow(label = "Application", value = "Intruder")

            SettingsDivider()

            InfoRow(label = "Version", value = versionName)
        }

        // The legal documents do not exist yet: the rows are listed as in the design but
        // are not interactive.
        SettingsCard(title = "Informations légales") {
            PendingRow("Politique de confidentialité")

            SettingsDivider()

            PendingRow("Mentions légales")

            SettingsDivider()

            PendingRow("Licences open source")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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

        Text(
            text = value,
            style = IntruderTypography.BodyMedium,
            color = IntruderColors.TextPrimary
        )
    }
}

@Composable
private fun PendingRow(label: String) {
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
            color = IntruderColors.TextTertiary
        )

        Text(
            text = "Bientôt",
            style = IntruderTypography.Label,
            color = IntruderColors.TextTertiary
        )
    }
}
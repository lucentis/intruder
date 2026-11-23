
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsPageHeader
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val versionName = runCatching {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
    }.getOrNull() ?: "Indisponible"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "À propos",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Intruder",
                    style = IntruderTypography.Headline,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Une application de protection qui surveille les tentatives de déverrouillage et capture des photos lors des événements détectés.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                HorizontalDivider(color = IntruderColors.Border)

                AboutInfoRow(
                    label = "Version",
                    value = versionName
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
                    text = "Informations",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Confidentialité",
                    style = IntruderTypography.BodyMedium,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Les photos sont conservées dans le stockage privé de l'application, sur votre appareil.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                HorizontalDivider(color = IntruderColors.Border)

                Text(
                    text = "Mentions légales",
                    style = IntruderTypography.BodyMedium,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Les informations légales seront disponibles dans une prochaine version.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AboutInfoRow(
    label: String,
    value: String
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
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

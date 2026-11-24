package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun SettingsMainScreen(
    overview: ProtectionOverview,
    onNavigate: (SettingsRoute) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Paramètres",
            style = IntruderTypography.Headline,
            color = IntruderColors.TextPrimary
        )

        HorizontalDivider(color = IntruderColors.Border)

        SectionTitle("Protection")

        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.VerifiedUser,
                title = "Protection globale",
                description = "Activer ou désactiver la protection",
                onClick = { onNavigate(SettingsRoute.GLOBAL_PROTECTION) },
                trailing = {
                    StatusBadge(
                        text = overview.global.badgeText(feminine = true),
                        kind = overview.global.badgeKind()
                    )
                }
            )

            SettingsDivider()

            SettingsNavRow(
                icon = Icons.Outlined.Visibility,
                title = "Surveillance",
                description = "Déverrouillages réussis et codes PIN échoués",
                onClick = { onNavigate(SettingsRoute.MONITORING) }
            )

            SettingsDivider()

            SettingsNavRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Capture photo",
                description = "Photos automatiques",
                onClick = { onNavigate(SettingsRoute.PHOTO_CAPTURE) }
            )
        }

        SectionTitle("Notifications")

        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications techniques",
                description = "Alertes et autorisations Android",
                onClick = { onNavigate(SettingsRoute.NOTIFICATIONS) }
            )
        }

        SectionTitle("Stockage")

        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.Storage,
                title = "Stockage local",
                description = "Espace utilisé et événements enregistrés",
                onClick = { onNavigate(SettingsRoute.STORAGE) }
            )
        }

        SectionTitle("À propos")

        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.Info,
                title = "Intruder",
                description = "Version et informations légales",
                onClick = { onNavigate(SettingsRoute.ABOUT) }
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = IntruderColors.TextPrimary
    )
}
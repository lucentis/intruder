package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.settings.SettingsDestination
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsEntry
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun SettingsHomeScreen(
    globalProtectionEnabled: Boolean,
    onNavigate: (SettingsDestination) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Paramètres",
                style = IntruderTypography.Headline,
                color = IntruderColors.TextPrimary
            )

            HorizontalDivider(color = IntruderColors.Border)
        }

        SettingsCategory(
            title = "Protection"
        ) {
            SettingsEntry(
                icon = Icons.Outlined.Security,
                title = "Protection globale",
                description = "Activer ou désactiver la protection",
                status = if (globalProtectionEnabled) "Activée" else "Désactivée",
                statusPositive = globalProtectionEnabled,
                onClick = {
                    onNavigate(SettingsDestination.GLOBAL_PROTECTION)
                }
            )

            SettingsEntry(
                icon = Icons.Outlined.Visibility,
                title = "Surveillance",
                description = "Déverrouillages réussis et codes PIN échoués",
                onClick = {
                    onNavigate(SettingsDestination.MONITORING)
                }
            )

            SettingsEntry(
                icon = Icons.Outlined.PhotoCamera,
                title = "Capture photo",
                description = "Photos automatiques",
                showDivider = false,
                onClick = {
                    onNavigate(SettingsDestination.PHOTO_CAPTURE)
                }
            )
        }

        SettingsCategory(
            title = "Notifications"
        ) {
            SettingsEntry(
                icon = Icons.Outlined.NotificationsNone,
                title = "Notifications techniques",
                description = "Alertes et autorisations Android",
                showDivider = false,
                onClick = {
                    onNavigate(SettingsDestination.TECHNICAL_NOTIFICATIONS)
                }
            )
        }

        SettingsCategory(
            title = "Stockage"
        ) {
            SettingsEntry(
                icon = Icons.Outlined.Storage,
                title = "Stockage local",
                description = "Espace utilisé et événements enregistrés",
                showDivider = false,
                onClick = {
                    onNavigate(SettingsDestination.LOCAL_STORAGE)
                }
            )
        }

        SettingsCategory(
            title = "À propos"
        ) {
            SettingsEntry(
                icon = Icons.Outlined.Info,
                title = "Intruder",
                description = "Version et informations légales",
                showDivider = false,
                onClick = {
                    onNavigate(SettingsDestination.ABOUT)
                }
            )
        }
    }
}

@Composable
private fun SettingsCategory(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = IntruderTypography.TitleSmall,
            color = IntruderColors.TextPrimary
        )

        SettingsCategoryCard {
            content()
        }
    }
}

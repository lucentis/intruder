
package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.lucentis.intruder.domain.model.AppSettings

private object SettingsRoute {
    const val HOME = "settings/home"
    const val GLOBAL_PROTECTION = "settings/global-protection"
    const val MONITORING = "settings/monitoring"
    const val PHOTO_CAPTURE = "settings/photo-capture"
    const val NOTIFICATIONS = "settings/notifications"
    const val STORAGE = "settings/storage"
    const val ABOUT = "settings/about"
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleGlobalProtection: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit,
    onToggleAutomaticCapture: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsRoute.HOME,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(SettingsRoute.HOME) {
            SettingsHomeScreen(navController)
        }

        composable(SettingsRoute.GLOBAL_PROTECTION) {
            GlobalProtectionScreen(
                settings = settings,
                onBack = { navController.popBackStack() },
                onToggleGlobalProtection = onToggleGlobalProtection
            )
        }

        composable(SettingsRoute.MONITORING) {
            MonitoringPlaceholder(
                settings = settings,
                onBack = { navController.popBackStack() },
                onToggleFailedAttempts = onToggleFailedAttempts,
                onToggleSuccessfulUnlocks = onToggleSuccessfulUnlocks
            )
        }

        composable(SettingsRoute.PHOTO_CAPTURE) {
            PlaceholderSettingsScreen(
                title = "Capture photo",
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsRoute.NOTIFICATIONS) {
            PlaceholderSettingsScreen(
                title = "Notifications techniques",
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsRoute.STORAGE) {
            PlaceholderSettingsScreen(
                title = "Stockage local",
                onBack = { navController.popBackStack() }
            )
        }

        composable(SettingsRoute.ABOUT) {
            PlaceholderSettingsScreen(
                title = "À propos",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun SettingsHomeScreen(
    navController: NavHostController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        SettingsCategory(title = "Protection") {
            SettingsEntry(
                symbol = "◉",
                title = "Protection globale",
                description = "État général de la protection",
                onClick = {
                    navController.navigate(SettingsRoute.GLOBAL_PROTECTION)
                }
            )

            SettingsEntry(
                symbol = "⌁",
                title = "Surveillance",
                description = "Événements surveillés",
                onClick = {
                    navController.navigate(SettingsRoute.MONITORING)
                }
            )

            SettingsEntry(
                symbol = "▣",
                title = "Capture photo",
                description = "Paramètres des photos automatiques",
                onClick = {
                    navController.navigate(SettingsRoute.PHOTO_CAPTURE)
                }
            )
        }

        SettingsCategory(title = "Notifications") {
            SettingsEntry(
                symbol = "◌",
                title = "Notifications techniques",
                description = "Alertes liées au fonctionnement",
                onClick = {
                    navController.navigate(SettingsRoute.NOTIFICATIONS)
                }
            )
        }

        SettingsCategory(title = "Stockage") {
            SettingsEntry(
                symbol = "▤",
                title = "Stockage local",
                description = "Espace utilisé par Intruder",
                onClick = {
                    navController.navigate(SettingsRoute.STORAGE)
                }
            )
        }

        SettingsCategory(title = "À propos") {
            SettingsEntry(
                symbol = "i",
                title = "À propos d'Intruder",
                description = "Version et informations légales",
                onClick = {
                    navController.navigate(SettingsRoute.ABOUT)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SettingsEntry(
    symbol: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                modifier = Modifier.size(38.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                androidx.compose.foundation.layout.Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GlobalProtectionScreen(
    settings: AppSettings,
    onBack: () -> Unit,
    onToggleGlobalProtection: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsBackHeader(
            title = "Protection globale",
            onBack = onBack
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Protection globale",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Activer ou désactiver la protection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = settings.globalProtectionEnabled,
                        onCheckedChange = { onToggleGlobalProtection() }
                    )
                }

                Text(
                    text = if (settings.globalProtectionEnabled) {
                        "Protection activée"
                    } else {
                        "Protection désactivée"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "État de la protection",
                    style = MaterialTheme.typography.titleMedium
                )

                ProtectionStatusRow(
                    title = "Déverrouillages réussis",
                    enabled = settings.globalProtectionEnabled &&
                        settings.monitorSuccessfulUnlocks
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProtectionStatusRow(
                    title = "Codes PIN échoués",
                    enabled = settings.globalProtectionEnabled &&
                        settings.monitorFailedAttempts
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ProtectionStatusRow(
                    title = "Capture photo",
                    enabled = settings.globalProtectionEnabled &&
                        settings.automaticCaptureEnabled
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Lorsque la protection globale est désactivée, la surveillance et les captures automatiques sont interrompues. Tes réglages individuels sont conservés.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = if (enabled) "Active" else "Inactive",
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun MonitoringPlaceholder(
    settings: AppSettings,
    onBack: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsBackHeader(
            title = "Surveillance",
            onBack = onBack
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingsSwitchRow(
                    title = "Déverrouillages réussis",
                    checked = settings.monitorSuccessfulUnlocks,
                    onCheckedChange = onToggleSuccessfulUnlocks
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                SettingsSwitchRow(
                    title = "Codes PIN échoués",
                    checked = settings.monitorFailedAttempts,
                    onCheckedChange = onToggleFailedAttempts
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )

        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

@Composable
private fun SettingsBackHeader(
    title: String,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) {
            Text("‹  Réglages")
        }

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun PlaceholderSettingsScreen(
    title: String,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsBackHeader(
            title = title,
            onBack = onBack
        )

        Text(
            text = "Cette page sera construite dans une prochaine étape.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

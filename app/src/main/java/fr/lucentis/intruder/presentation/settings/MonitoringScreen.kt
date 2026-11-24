package fr.lucentis.intruder.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.runtime.Composable
import fr.lucentis.intruder.domain.model.AppSettings

@Composable
fun MonitoringScreen(
    settings: AppSettings,
    isAdminActive: Boolean,
    onUpdate: SettingsUpdate,
    onActivateAdmin: () -> Unit,
    onBack: () -> Unit
) {
    SettingsSubScreen(title = "Surveillance", onBack = onBack) {
        SettingsCard(title = "Événements surveillés") {
            SettingsSwitchRow(
                icon = Icons.Outlined.LockOpen,
                title = "Déverrouillages réussis",
                description = "Détecter les déverrouillages réussis du téléphone.",
                checked = settings.monitorSuccessfulUnlocks,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(monitorSuccessfulUnlocks = checked) }
                }
            )

            SettingsDivider()

            SettingsSwitchRow(
                icon = Icons.Outlined.Lock,
                title = "Codes PIN échoués",
                description = "Détecter les tentatives de déverrouillage échouées.",
                checked = settings.monitorFailedAttempts,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(monitorFailedAttempts = checked) }
                }
            )

            if (settings.monitorFailedAttempts) {
                PermissionNotice(
                    granted = isAdminActive,
                    grantedLabel = "Administrateur de l'appareil : autorisé",
                    warning = "L'autorisation Administrateur de l'appareil est nécessaire " +
                        "pour détecter les codes PIN échoués."
                ) {
                    WarningAction(label = "Autoriser", onClick = onActivateAdmin)
                }
            }
        }

        SettingsInfoCard(
            text = "Chaque type de surveillance peut être activé ou désactivé " +
                "indépendamment."
        )
    }
}
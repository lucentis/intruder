package fr.lucentis.intruder.presentation.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.runtime.Composable
import fr.lucentis.intruder.domain.model.AppSettings

@Composable
fun TechnicalNotificationsScreen(
    settings: AppSettings,
    notificationsEnabled: Boolean,
    onUpdate: SettingsUpdate,
    onOpenNotificationSettings: () -> Unit,
    onBack: () -> Unit
) {
    val anyNotificationEnabled = settings.notifyProtectionInterrupted ||
        settings.notifyMissingPermission ||
        settings.notifyCaptureProblem

    SettingsSubScreen(title = "Notifications techniques", onBack = onBack) {
        SettingsCard(title = "Types de notifications") {
            SettingsSwitchRow(
                icon = Icons.Outlined.Security,
                title = "Protection interrompue",
                description = "Signaler lorsque le service de protection s'arrête.",
                checked = settings.notifyProtectionInterrupted,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(notifyProtectionInterrupted = checked) }
                }
            )

            SettingsDivider()

            SettingsSwitchRow(
                icon = Icons.Outlined.VpnKey,
                title = "Autorisation manquante",
                description = "Signaler lorsqu'une autorisation nécessaire n'est plus " +
                    "disponible.",
                checked = settings.notifyMissingPermission,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(notifyMissingPermission = checked) }
                }
            )

            SettingsDivider()

            SettingsSwitchRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Problème de capture",
                description = "Signaler lorsqu'une photo n'a pas pu être capturée ou " +
                    "enregistrée.",
                checked = settings.notifyCaptureProblem,
                onCheckedChange = { checked ->
                    onUpdate { it.copy(notifyCaptureProblem = checked) }
                }
            )

            if (anyNotificationEnabled) {
                PermissionNotice(
                    granted = notificationsEnabled,
                    grantedLabel = "Notifications autorisées dans Android",
                    warning = "Les notifications sont bloquées dans Android. Les problèmes " +
                        "restent enregistrés, mais aucune alerte ne s'affichera."
                ) {
                    WarningAction(
                        label = "Ouvrir les paramètres Android",
                        onClick = onOpenNotificationSettings
                    )
                }
            }
        }

        SettingsInfoCard(
            text = "Chaque type de notification peut être activé ou désactivé " +
                "indépendamment."
        )
    }
}
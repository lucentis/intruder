
package fr.lucentis.intruder.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.model.AppSettings

@Composable
fun SettingsScreen(
    settings: AppSettings,
    isAdminActive: Boolean,
    hasCameraPermission: Boolean,
    hasTestEvent: Boolean,
    onActivateAdmin: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onToggleAutomaticCapture: () -> Unit,
    onToggleGlobalProtection: () -> Unit,
    onToggleFailedAttempts: () -> Unit,
    onToggleSuccessfulUnlocks: () -> Unit,
    onAddTestEvent: () -> Unit,
    onDeleteTestEvent: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineMedium
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Protection", style = MaterialTheme.typography.titleMedium)

                Text(
                    "Protection globale : ${
                        if (settings.globalProtectionEnabled) "Activée" else "Désactivée"
                    }"
                )

                Button(
                    onClick = onToggleGlobalProtection,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.globalProtectionEnabled) {
                            "Désactiver la protection globale"
                        } else {
                            "Activer la protection globale"
                        }
                    )
                }

                Text(
                    "Surveillance des échecs : ${
                        if (settings.monitorFailedAttempts) "Activée" else "Désactivée"
                    }"
                )

                Button(
                    onClick = onToggleFailedAttempts,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.monitorFailedAttempts) {
                            "Désactiver la surveillance des échecs"
                        } else {
                            "Activer la surveillance des échecs"
                        }
                    )
                }

                Text(
                    "Surveillance des déverrouillages réussis : ${
                        if (settings.monitorSuccessfulUnlocks) "Activée" else "Désactivée"
                    }"
                )

                Button(
                    onClick = onToggleSuccessfulUnlocks,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.monitorSuccessfulUnlocks) {
                            "Désactiver la surveillance des succès"
                        } else {
                            "Activer la surveillance des succès"
                        }
                    )
                }

                Text(
                    "Capture automatique : ${
                        if (settings.automaticCaptureEnabled) "Activée" else "Désactivée"
                    }"
                )

                Button(
                    onClick = onToggleAutomaticCapture,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (settings.automaticCaptureEnabled) {
                            "Désactiver la capture automatique"
                        } else {
                            "Activer la capture automatique"
                        }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Autorisations Android", style = MaterialTheme.typography.titleMedium)

                Text(
                    "Administrateur de l'appareil : ${
                        if (isAdminActive) "Activé" else "Désactivé"
                    }"
                )

                Button(
                    onClick = onActivateAdmin,
                    enabled = !isAdminActive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activer l'administrateur")
                }

                Text(
                    "Permission caméra : ${
                        if (hasCameraPermission) "Accordée" else "Manquante"
                    }"
                )

                Button(
                    onClick = onRequestCameraPermission,
                    enabled = !hasCameraPermission,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (hasCameraPermission) {
                            "Autorisation caméra accordée"
                        } else {
                            "Autoriser l'accès à la caméra"
                        }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Outils de test", style = MaterialTheme.typography.titleMedium)

                Button(
                    onClick = onAddTestEvent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ajouter un événement de test")
                }

                Button(
                    onClick = onDeleteTestEvent,
                    enabled = hasTestEvent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Supprimer le dernier événement de test")
                }
            }
        }
    }
}

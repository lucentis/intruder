
package fr.lucentis.intruder.presentation.debug

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.model.AppSettings
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event

data class DiagnosticInfo(
    val eventType: String,
    val detectedAt: String,
    val processing: String,
    val captureFailure: String?
)

@Composable
fun DebugScreen(
    latestEvent: Event?,
    diagnostic: DiagnosticInfo?,
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
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Debug",
            style = MaterialTheme.typography.headlineMedium
        )

        // Dernier événement enregistré dans Room
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Dernier événement",
                    style = MaterialTheme.typography.titleMedium
                )

                latestEvent?.let { event ->
                    Text("Type : ${event.type}")
                    Text("Capture : ${event.captureStatus}")
                    Text("Raison : ${event.failureReason ?: "Aucune"}")
                    Text("Date : ${event.timestamp}")

                    if (
                        event.captureStatus == CaptureStatus.SUCCESS &&
                        event.photoPath != null
                    ) {
                        val bitmap = remember(event.photoPath) {
                            BitmapFactory.decodeFile(event.photoPath)
                        }

                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Photo du dernier événement",
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(240.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Text("La photo est introuvable ou illisible.")
                        }
                    } else {
                        Text(
                            when (event.captureStatus) {
                                CaptureStatus.FAILED ->
                                    "La prise de photo a échoué."

                                CaptureStatus.SKIPPED ->
                                    "Aucune photo n'a été prise."

                                CaptureStatus.SUCCESS ->
                                    "Aucune photo associée."
                            }
                        )
                    }
                } ?: Text("Aucun événement enregistré.")
            }
        }

        // Dernier diagnostic conservé par EventProcessor
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Dernier diagnostic technique",
                    style = MaterialTheme.typography.titleMedium
                )

                diagnostic?.let { result ->
                    Text("Événement traité : ${result.eventType}")
                    Text("Date de détection : ${result.detectedAt}")
                    Text("Traitement : ${result.processing}")

                    result.captureFailure?.let {
                        Text("Échec de capture : oui")
                        Text(it)
                    } ?: Text("Échec de capture : aucun.")
                } ?: Text("Aucun diagnostic disponible.")
            }
        }

        // Anciennes commandes de réglage, conservées pour les tests.
        Text(
            text = "Réglages de test",
            style = MaterialTheme.typography.titleLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Protection",
                    style = MaterialTheme.typography.titleMedium
                )

                DebugSwitchRow(
                    title = "Protection globale",
                    checked = settings.globalProtectionEnabled,
                    onCheckedChange = onToggleGlobalProtection
                )

                DebugSwitchRow(
                    title = "Surveillance des codes PIN échoués",
                    checked = settings.monitorFailedAttempts,
                    onCheckedChange = onToggleFailedAttempts
                )

                DebugSwitchRow(
                    title = "Surveillance des déverrouillages réussis",
                    checked = settings.monitorSuccessfulUnlocks,
                    onCheckedChange = onToggleSuccessfulUnlocks
                )

                DebugSwitchRow(
                    title = "Capture automatique",
                    checked = settings.automaticCaptureEnabled,
                    onCheckedChange = onToggleAutomaticCapture
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Autorisations Android",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    "Administrateur de l'appareil : " +
                        if (isAdminActive) "Activé" else "Désactivé"
                )

                Button(
                    onClick = onActivateAdmin,
                    enabled = !isAdminActive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Activer l'administrateur")
                }

                Text(
                    "Permission caméra : " +
                        if (hasCameraPermission) "Accordée" else "Manquante"
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
                Text(
                    "Outils de test",
                    style = MaterialTheme.typography.titleMedium
                )

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

@Composable
private fun DebugSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
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

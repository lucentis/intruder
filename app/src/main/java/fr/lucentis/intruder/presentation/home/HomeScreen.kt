
package fr.lucentis.intruder.presentation.home

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
fun HomeScreen(
    settings: AppSettings,
    isAdminActive: Boolean,
    hasCameraPermission: Boolean,
    eventCount: Int,
    latestEvent: Event?,
    diagnostic: DiagnosticInfo?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Intruder",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Vue d'ensemble",
            style = MaterialTheme.typography.titleLarge
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("État de la protection", style = MaterialTheme.typography.titleMedium)

                Text(
                    "Protection globale : ${
                        if (settings.globalProtectionEnabled) "Activée" else "Désactivée"
                    }"
                )
                Text(
                    "Surveillance des échecs : ${
                        if (settings.monitorFailedAttempts) "Activée" else "Désactivée"
                    }"
                )
                Text(
                    "Capture automatique : ${
                        if (settings.automaticCaptureEnabled) "Activée" else "Désactivée"
                    }"
                )
                Text(
                    "Administrateur Android : ${
                        if (isAdminActive) "Activé" else "Désactivé"
                    }"
                )
                Text(
                    "Permission caméra : ${
                        if (hasCameraPermission) "Accordée" else "Manquante"
                    }"
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Événements", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = eventCount.toString(),
                    style = MaterialTheme.typography.headlineLarge
                )
                Text("Événement(s) enregistré(s) dans Room.")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dernier événement", style = MaterialTheme.typography.titleMedium)

                latestEvent?.let { event ->
                    Text("Type : ${event.type}")
                    Text("Capture : ${event.captureStatus}")
                    Text("Raison : ${event.failureReason ?: "Aucune"}")
                    Text("Date : ${event.timestamp}")

                    if (event.captureStatus == CaptureStatus.SUCCESS &&
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
                                CaptureStatus.FAILED -> "La prise de photo a échoué."
                                CaptureStatus.SKIPPED -> "Aucune photo n'a été prise."
                                CaptureStatus.SUCCESS -> "Aucune photo associée."
                            }
                        )
                    }
                } ?: Text("Aucun événement enregistré.")
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Dernier diagnostic technique", style = MaterialTheme.typography.titleMedium)

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

        Spacer(modifier = Modifier.size(8.dp))
    }
}

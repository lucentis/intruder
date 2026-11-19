
package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.lucentis.intruder.domain.model.CaptureFailureReason
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventDetailScreen(
    event: Event,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val dateTime = SimpleDateFormat(
        "dd/MM/yyyy 'à' HH:mm:ss",
        Locale.getDefault()
    ).format(Date(event.timestamp))

    val eventType = when (event.type) {
        EventType.UNLOCK_SUCCESS -> "Déverrouillage réussi"
        EventType.UNLOCK_FAILED -> "Tentative échouée"
    }

    val captureStatus = when (event.captureStatus) {
        CaptureStatus.SUCCESS -> "Photo enregistrée"
        CaptureStatus.FAILED -> "Échec de la capture"
        CaptureStatus.SKIPPED -> "Capture ignorée"
    }

    val failureReason = when (event.failureReason) {
        CaptureFailureReason.PERMISSION_MISSING ->
            "Permission caméra manquante"

        CaptureFailureReason.CAMERA_UNAVAILABLE ->
            "Caméra indisponible"

        CaptureFailureReason.CAMERA_BUSY ->
            "Caméra occupée"

        CaptureFailureReason.CAMERA_DISCONNECTED ->
            "Caméra déconnectée"

        CaptureFailureReason.CAMERA_ERROR ->
            "Erreur de caméra"

        CaptureFailureReason.CAPTURE_FAILED ->
            "La prise de photo a échoué"

        CaptureFailureReason.SAVE_FAILED ->
            "L'enregistrement de la photo a échoué"

        null -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GalleryHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (event.photoPath != null) {
                AsyncImage(
                    model = File(event.photoPath),
                    contentDescription = "Photo de l'événement",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp, max = 420.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aucune photo disponible",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = eventType,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = dateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "État de la capture",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = captureStatus,
                    style = MaterialTheme.typography.bodyLarge
                )

                if (failureReason != null) {
                    Text(
                        text = failureReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            TextButton(
                onClick = {
                    showDeleteConfirmation = true
                }
            ) {
                Text(
                    text = "Supprimer",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Supprimer cet événement ?")
            },
            text = {
                Text(
                    "Voulez-vous supprimer définitivement cet événement " +
                        "ainsi que la photo associée ?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    }
                ) {
                    Text("Supprimer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }
}

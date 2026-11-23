package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import java.io.File

@Composable
fun EventDetailScreen(
    event: Event,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DetailTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventPhoto(event = event)

            EventSummary(event = event)

            CaptureStatusCard(event = event)

            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntruderColors.Surface,
                    contentColor = IntruderColors.Error
                ),
                border = BorderStroke(1.dp, IntruderColors.Border)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Supprimer cet événement",
                    style = IntruderTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        DeleteEventsDialog(
            count = 1,
            onConfirm = {
                showDeleteConfirmation = false
                onDelete()
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun DetailTopBar(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    modifier = Modifier.size(24.dp),
                    tint = IntruderColors.TextPrimary
                )
            }

            Text(
                text = "Événement",
                modifier = Modifier.padding(start = 4.dp),
                style = IntruderTypography.Title,
                color = IntruderColors.TextPrimary
            )
        }

        HorizontalDivider(color = IntruderColors.Border)
    }
}

@Composable
private fun EventPhoto(event: Event) {
    val photoPath = event.photoPath

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 220.dp, max = 420.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(IntruderColors.SurfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (photoPath != null) {
            // Fit shows the whole picture; the letterbox takes the secondary surface color.
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Photo de l'événement",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = IntruderColors.TextTertiary
                )

                Text(
                    text = "Aucune photo disponible",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EventSummary(event: Event) {
    val dateTime = remember(event.timestamp) { formatDateTime(event.timestamp) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = event.type.displayName(),
            style = IntruderTypography.Headline,
            color = IntruderColors.TextPrimary
        )

        Text(
            text = dateTime,
            style = IntruderTypography.Body,
            color = IntruderColors.TextSecondary
        )
    }
}

// The status is always written and paired with an icon, so it never relies on color alone.
@Composable
private fun CaptureStatusCard(event: Event) {
    val statusColor = when (event.captureStatus) {
        CaptureStatus.SUCCESS -> IntruderColors.Success
        CaptureStatus.FAILED -> IntruderColors.Error
        CaptureStatus.SKIPPED -> IntruderColors.TextSecondary
    }

    val statusIcon = when (event.captureStatus) {
        CaptureStatus.SUCCESS -> Icons.Outlined.CheckCircle
        CaptureStatus.FAILED -> Icons.Outlined.ErrorOutline
        CaptureStatus.SKIPPED -> Icons.Outlined.RemoveCircleOutline
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(IntruderColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "État de la capture",
            style = IntruderTypography.TitleSmall,
            color = IntruderColors.TextPrimary
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = statusColor
            )

            Text(
                text = event.captureStatus.displayName(),
                style = IntruderTypography.BodyMedium,
                color = statusColor
            )
        }

        event.failureReason?.let { reason ->
            HorizontalDivider(color = IntruderColors.Border)

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Motif",
                    style = IntruderTypography.Label,
                    color = IntruderColors.TextSecondary
                )

                Text(
                    text = reason.displayName(),
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextPrimary
                )
            }
        }
    }
}
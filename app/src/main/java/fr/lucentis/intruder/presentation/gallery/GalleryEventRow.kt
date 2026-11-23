package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.lucentis.intruder.domain.model.CaptureStatus
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import java.io.File

@Composable
fun GalleryEventRow(
    event: Event,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val time = remember(event.timestamp) { formatTime(event.timestamp) }
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(IntruderColors.Surface)
            .border(
                width = 1.dp,
                color = if (isSelected) IntruderColors.Primary else Color.Transparent,
                shape = shape
            )
            .semantics {
                contentDescription = "${event.type.displayName()}, $time, " +
                    event.captureStatus.displayName()
                if (isSelectionMode) selected = isSelected
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(PhotoPlaceholderBrush),
            contentAlignment = Alignment.Center
        ) {
            val photoPath = event.photoPath

            if (photoPath != null) {
                AsyncImage(
                    model = File(photoPath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = IntruderColors.TextTertiary
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = event.type.displayName(),
                style = IntruderTypography.TitleSmall,
                color = IntruderColors.TextPrimary
            )

            Text(
                text = time,
                style = IntruderTypography.Label,
                color = IntruderColors.TextSecondary
            )

            Text(
                text = event.captureStatus.displayName(),
                style = IntruderTypography.Label,
                color = if (event.captureStatus == CaptureStatus.FAILED) {
                    IntruderColors.Error
                } else {
                    IntruderColors.TextSecondary
                }
            )
        }

        if (isSelectionMode) {
            SelectionIndicator(selected = isSelected)
        }
    }
}
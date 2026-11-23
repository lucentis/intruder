package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import java.io.File

internal val PhotoPlaceholderBrush = Brush.linearGradient(
    listOf(
        IntruderColors.PhotoPlaceholderStart,
        IntruderColors.PhotoPlaceholderEnd
    )
)

@Composable
fun GalleryEventItem(
    event: Event,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val time = remember(event.timestamp) { formatTime(event.timestamp) }
    val description = "${event.type.displayName()}, $time"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(PhotoPlaceholderBrush)
            .semantics {
                contentDescription = description
                if (isSelectionMode) selected = isSelected
            }
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        val photoPath = event.photoPath

        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(28.dp),
                tint = IntruderColors.TextTertiary
            )
        }

        // The badge and the time sit on top of arbitrary photos. A soft dark gradient keeps
        // the white text readable on bright pictures.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f))
                    )
                )
        )

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EventTypeBadge(type = event.type)

            Text(
                text = time,
                style = IntruderTypography.Caption,
                color = Color.White
            )
        }

        if (isSelectionMode) {
            SelectionIndicator(
                selected = isSelected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            )
        }
    }
}

// Success is neutral and failure is red, so that failures stand out without making the
// whole grid colorful.
@Composable
private fun EventTypeBadge(type: EventType) {
    val color = if (type == EventType.UNLOCK_SUCCESS) {
        IntruderColors.TextSecondary.copy(alpha = 0.92f)
    } else {
        IntruderColors.Error.copy(alpha = 0.95f)
    }

    Text(
        text = type.badgeLabel(),
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = IntruderTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
        color = Color.White
    )
}

@Composable
internal fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (selected) IntruderColors.Primary else IntruderColors.Surface)
            .border(
                width = 1.5.dp,
                color = if (selected) IntruderColors.Surface else IntruderColors.Primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = IntruderColors.OnPrimary
            )
        }
    }
}
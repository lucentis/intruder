
package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.domain.model.EventType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryEventItem(
    event: Event,
    modifier: Modifier = Modifier
) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(event.timestamp))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        val photoPath = event.photoPath

        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Photo de l'événement",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = when (event.type) {
                EventType.UNLOCK_SUCCESS -> "Succès"
                EventType.UNLOCK_FAILED -> "Échec"
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .background(Color.Black.copy(alpha = 0.65f)),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )

        Text(
            text = time,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.Black.copy(alpha = 0.65f)),
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

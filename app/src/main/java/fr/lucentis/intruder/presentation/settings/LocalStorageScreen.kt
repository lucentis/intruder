package fr.lucentis.intruder.presentation.settings

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.platform.storage.StorageUsage
import fr.lucentis.intruder.platform.storage.readStorageUsage
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocalStorageScreen(
    eventCount: Int,
    onOpenGallery: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var usage by remember { mutableStateOf<StorageUsage?>(null) }

    // Walking the files is disk work: it runs off the main thread, and again whenever the
    // number of events changes.
    LaunchedEffect(eventCount) {
        usage = withContext(Dispatchers.IO) { readStorageUsage(context) }
    }

    val usedText = usage?.let { Formatter.formatShortFileSize(context, it.usedBytes) } ?: "-"

    // Share of the device storage used by the app. It is tiny, so the filled part never
    // gets narrower than a small visible stub.
    val fraction = usage?.let {
        if (it.deviceTotalBytes > 0) {
            (it.usedBytes.toFloat() / it.deviceTotalBytes).coerceIn(0.03f, 1f)
        } else {
            0.03f
        }
    } ?: 0f

    SettingsSubScreen(title = "Stockage local", onBack = onBack) {
        SettingsCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SettingsIconTile(icon = Icons.Outlined.Storage)

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Stockage des photos",
                        style = IntruderTypography.TitleSmall,
                        color = IntruderColors.TextPrimary
                    )

                    Text(
                        text = "Les photos sont conservées localement sur cet appareil.",
                        style = IntruderTypography.Label,
                        color = IntruderColors.TextSecondary
                    )
                }
            }

            SettingsDivider()

            Column(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Espace utilisé",
                        style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = IntruderColors.TextPrimary
                    )

                    Text(
                        text = usedText,
                        style = IntruderTypography.Title,
                        color = IntruderColors.TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape)
                        .background(IntruderColors.Border)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(IntruderColors.Primary)
                    )
                }

                Text(
                    text = "Espace occupé par les données d'Intruder.",
                    style = IntruderTypography.Label,
                    color = IntruderColors.TextSecondary
                )
            }

            SettingsDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Événements enregistrés",
                    style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = eventCount.toString(),
                    style = IntruderTypography.Title,
                    color = IntruderColors.TextPrimary
                )
            }

            SettingsDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = IntruderColors.Primary
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Stockage privé",
                        style = IntruderTypography.TitleSmall,
                        color = IntruderColors.TextPrimary
                    )

                    Text(
                        text = "Les photos sont conservées dans l'espace privé de " +
                            "l'application.",
                        style = IntruderTypography.Label,
                        color = IntruderColors.TextSecondary
                    )
                }
            }
        }

        SettingsCard {
            SettingsNavRow(
                icon = Icons.Outlined.PhotoLibrary,
                title = "Gérer les événements",
                description = "Ouvrir la galerie pour supprimer des événements et leurs photos.",
                onClick = onOpenGallery
            )
        }

        SettingsInfoCard(
            text = "Les photos sont conservées sur l'appareil et ne sont pas " +
                "automatiquement supprimées."
        )
    }
}
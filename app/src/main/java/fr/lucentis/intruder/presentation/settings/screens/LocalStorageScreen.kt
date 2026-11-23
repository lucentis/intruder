
package fr.lucentis.intruder.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.presentation.settings.components.SettingsCategoryCard
import fr.lucentis.intruder.presentation.settings.components.SettingsPageHeader
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LocalStorageScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val photosDirectory = File(context.filesDir, "captures")

    val storageUsedBytes = produceState(
        initialValue = 0L,
        key1 = photosDirectory
    ) {
        value = withContext(Dispatchers.IO) {
            photosDirectory.calculateDirectorySize()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SettingsPageHeader(
            title = "Stockage local",
            onBack = onBack
        )

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Stockage des photos",
                        style = IntruderTypography.TitleSmall,
                        color = IntruderColors.TextPrimary
                    )

                    Text(
                        text = "Les photos sont conservées localement sur cet appareil.",
                        style = IntruderTypography.Body,
                        color = IntruderColors.TextSecondary
                    )
                }

                HorizontalDivider(color = IntruderColors.Border)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Espace utilisé",
                            style = IntruderTypography.BodyMedium,
                            color = IntruderColors.TextPrimary
                        )

                        Text(
                            text = formatStorageSize(storageUsedBytes.value),
                            style = IntruderTypography.Title,
                            color = IntruderColors.TextPrimary
                        )
                    }

                    Text(
                        text = "Espace occupé par les photos enregistrées par Intruder.",
                        style = IntruderTypography.Caption,
                        color = IntruderColors.TextSecondary
                    )
                }

                HorizontalDivider(color = IntruderColors.Border)

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Stockage privé",
                        style = IntruderTypography.BodyMedium,
                        color = IntruderColors.TextPrimary
                    )

                    Text(
                        text = "Les photos sont conservées dans l'espace privé de l'application. Elles ne sont pas accessibles aux autres applications.",
                        style = IntruderTypography.Body,
                        color = IntruderColors.TextSecondary
                    )
                }
            }
        }

        SettingsCategoryCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "À savoir",
                    style = IntruderTypography.TitleSmall,
                    color = IntruderColors.TextPrimary
                )

                Text(
                    text = "Les photos restent conservées sur cet appareil. Elles ne sont pas automatiquement supprimées.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )

                Text(
                    text = "La désinstallation d'Intruder entraîne la suppression de ses données privées, y compris les photos.",
                    style = IntruderTypography.Body,
                    color = IntruderColors.TextSecondary
                )
            }
        }
    }
}

private fun File.calculateDirectorySize(): Long {
    if (!exists() || !isDirectory) return 0L

    return walkTopDown()
        .filter { it.isFile }
        .sumOf { it.length() }
}

private fun formatStorageSize(bytes: Long): String {
    if (bytes <= 0L) return "0 Mo"

    val kilobytes = bytes / 1024.0
    val megabytes = kilobytes / 1024.0
    val gigabytes = megabytes / 1024.0

    return when {
        gigabytes >= 1.0 -> "%.2f Go".format(java.util.Locale.FRANCE, gigabytes)
        megabytes >= 1.0 -> "%.1f Mo".format(java.util.Locale.FRANCE, megabytes)
        kilobytes >= 1.0 -> "%.0f Ko".format(java.util.Locale.FRANCE, kilobytes)
        else -> "$bytes octets"
    }
}

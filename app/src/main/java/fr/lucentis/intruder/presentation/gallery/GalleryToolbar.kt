
package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GalleryToolbar(
    selectedFilter: GalleryFilter,
    onFilterChange: (GalleryFilter) -> Unit,
    displayMode: GalleryDisplayMode,
    onDisplayModeChange: (GalleryDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalleryTypeFilter(
                label = "Tous",
                selected = selectedFilter == GalleryFilter.ALL,
                onClick = { onFilterChange(GalleryFilter.ALL) }
            )

            GalleryTypeFilter(
                label = "Succès",
                selected = selectedFilter == GalleryFilter.SUCCESS,
                onClick = { onFilterChange(GalleryFilter.SUCCESS) }
            )

            GalleryTypeFilter(
                label = "Échecs",
                selected = selectedFilter == GalleryFilter.FAILED,
                onClick = { onFilterChange(GalleryFilter.FAILED) }
            )
        }

        Row(
            modifier = Modifier
                .height(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IntruderColors.SurfaceVariant)
                .padding(1.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalleryDisplayModeButton(
                mode = GalleryDisplayMode.GRID,
                selected = displayMode == GalleryDisplayMode.GRID,
                onClick = {
                    onDisplayModeChange(GalleryDisplayMode.GRID)
                }
            )

            GalleryDisplayModeButton(
                mode = GalleryDisplayMode.LIST,
                selected = displayMode == GalleryDisplayMode.LIST,
                onClick = {
                    onDisplayModeChange(GalleryDisplayMode.LIST)
                }
            )
        }
    }
}

@Composable
private fun GalleryTypeFilter(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = IntruderTypography.Label
            )
        },
        shape = RoundedCornerShape(999.dp),
        border = null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = IntruderColors.Primary,
            selectedLabelColor = IntruderColors.OnPrimary,
            containerColor = IntruderColors.SurfaceVariant,
            labelColor = IntruderColors.PrimaryVariant
        )
    )
}

@Composable
private fun GalleryDisplayModeButton(
    mode: GalleryDisplayMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(7.dp)

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(shape)
            .background(
                color = if (selected) {
                    IntruderColors.Primary
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(18.dp)
        ) {
            val iconColor = if (selected) {
                IntruderColors.OnPrimary
            } else {
                IntruderColors.PrimaryVariant
            }

            val stroke = 1.5.dp.toPx()

            if (mode == GalleryDisplayMode.GRID) {
                val cellSize = 5.dp.toPx()
                val gap = 2.5.dp.toPx()
                val totalSize = cellSize * 2 + gap
                val start = (size.width - totalSize) / 2

                for (row in 0..1) {
                    for (column in 0..1) {
                        drawRect(
                            color = iconColor,
                            topLeft = Offset(
                                x = start + column * (cellSize + gap),
                                y = start + row * (cellSize + gap)
                            ),
                            size = Size(cellSize, cellSize),
                            style = Stroke(stroke)
                        )
                    }
                }
            } else {
                val bulletX = 2.dp.toPx()
                val lineStart = 6.dp.toPx()
                val lineEnd = 16.dp.toPx()

                for (row in 0..2) {
                    val y = (3 + row * 6).dp.toPx()

                    drawCircle(
                        color = iconColor,
                        radius = 1.dp.toPx(),
                        center = Offset(bulletX, y)
                    )

                    drawLine(
                        color = iconColor,
                        start = Offset(lineStart, y),
                        end = Offset(lineEnd, y),
                        strokeWidth = stroke
                    )
                }
            }
        }
    }
}

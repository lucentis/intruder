package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography

@Composable
fun GalleryToolbar(
    selectedFilter: GalleryFilter,
    onFilterChange: (GalleryFilter) -> Unit,
    isPeriodActive: Boolean,
    onPeriodClick: () -> Unit,
    displayMode: GalleryDisplayMode,
    onDisplayModeChange: (GalleryDisplayMode) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scrolls instead of overflowing on narrow screens or large font sizes.
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
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

        GalleryPeriodButton(
            active = isPeriodActive,
            onClick = onPeriodClick
        )

        Row(
            modifier = Modifier
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IntruderColors.SurfaceVariant)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GalleryDisplayModeButton(
                mode = GalleryDisplayMode.GRID,
                selected = displayMode == GalleryDisplayMode.GRID,
                onClick = { onDisplayModeChange(GalleryDisplayMode.GRID) }
            )

            GalleryDisplayModeButton(
                mode = GalleryDisplayMode.LIST,
                selected = displayMode == GalleryDisplayMode.LIST,
                onClick = { onDisplayModeChange(GalleryDisplayMode.LIST) }
            )
        }
    }
}

// Shown under the toolbar while a period is applied, so that the cumulated filters stay
// identifiable. Tapping it clears the period.
@Composable
fun GalleryActivePeriod(
    label: String,
    onClear: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(IntruderColors.Primary)
            .clickable(
                onClickLabel = "Supprimer le filtre de période",
                onClick = onClear
            )
            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = IntruderTypography.Label,
            color = IntruderColors.OnPrimary
        )

        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = IntruderColors.OnPrimary
        )
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
private fun GalleryPeriodButton(
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (active) IntruderColors.Primary else IntruderColors.SurfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = if (active) {
                "Modifier la période"
            } else {
                "Filtrer par période"
            },
            modifier = Modifier.size(20.dp),
            tint = if (active) IntruderColors.OnPrimary else IntruderColors.PrimaryVariant
        )
    }
}

@Composable
private fun GalleryDisplayModeButton(
    mode: GalleryDisplayMode,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                color = if (selected) IntruderColors.Primary else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (mode == GalleryDisplayMode.GRID) {
                Icons.Filled.GridView
            } else {
                Icons.AutoMirrored.Filled.ViewList
            },
            contentDescription = if (mode == GalleryDisplayMode.GRID) {
                "Affichage en grille"
            } else {
                "Affichage en liste"
            },
            modifier = Modifier.size(18.dp),
            tint = if (selected) IntruderColors.OnPrimary else IntruderColors.PrimaryVariant
        )
    }
}
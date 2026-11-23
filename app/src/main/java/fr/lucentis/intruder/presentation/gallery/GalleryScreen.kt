package fr.lucentis.intruder.presentation.gallery

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.domain.model.Event
import fr.lucentis.intruder.presentation.theme.IntruderColors
import fr.lucentis.intruder.presentation.theme.IntruderTypography
import java.time.LocalDate

private val SelectedIdsSaver = listSaver<Set<Long>, Long>(
    save = { it.toList() },
    restore = { it.toSet() }
)

// An empty list stands for "no period".
private val PeriodSaver = listSaver<GalleryPeriod?, Long>(
    save = { period ->
        if (period == null) {
            emptyList()
        } else {
            listOf(period.start.toEpochDay(), period.end.toEpochDay())
        }
    },
    restore = { values ->
        if (values.size == 2) {
            GalleryPeriod(
                start = LocalDate.ofEpochDay(values[0]),
                end = LocalDate.ofEpochDay(values[1])
            )
        } else {
            null
        }
    }
)

@Composable
fun GalleryScreen(
    events: List<Event>,
    displayMode: GalleryDisplayMode,
    onDisplayModeChange: (GalleryDisplayMode) -> Unit,
    onDeleteEvents: (List<Long>) -> Unit = {},
    onEventClick: (Event) -> Unit = {},
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(GalleryFilter.ALL)
    }

    var period by rememberSaveable(stateSaver = PeriodSaver) {
        mutableStateOf<GalleryPeriod?>(null)
    }

    var showPeriodPicker by rememberSaveable {
        mutableStateOf(false)
    }

    var selectedEventIds by rememberSaveable(stateSaver = SelectedIdsSaver) {
        mutableStateOf(emptySet<Long>())
    }

    var showDeleteConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    val filteredEvents = remember(events, selectedFilter, period) {
        events
            .filterByGalleryFilter(selectedFilter)
            .filterByPeriod(period)
    }
    val dateGroups = remember(filteredEvents) { filteredEvents.groupByDate() }

    // Only the selected events that are still displayed count. Changing a filter or
    // deleting an event elsewhere therefore can never leave a hidden event selected, and
    // "Supprimer" only ever deletes what the user can see.
    val visibleSelection = remember(selectedEventIds, filteredEvents) {
        filteredEvents.map { it.id }.filter { it in selectedEventIds }.toSet()
    }
    val isSelectionMode = visibleSelection.isNotEmpty()

    LaunchedEffect(visibleSelection) {
        if (visibleSelection != selectedEventIds) {
            selectedEventIds = visibleSelection
        }
    }

    BackHandler(enabled = isSelectionMode) {
        selectedEventIds = emptySet()
    }

    fun toggleSelection(eventId: Long) {
        selectedEventIds = if (eventId in selectedEventIds) {
            selectedEventIds - eventId
        } else {
            selectedEventIds + eventId
        }
    }

    fun clearFilters() {
        selectedFilter = GalleryFilter.ALL
        period = null
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GalleryHeader()

            GalleryToolbar(
                selectedFilter = selectedFilter,
                onFilterChange = { selectedFilter = it },
                isPeriodActive = period != null,
                onPeriodClick = { showPeriodPicker = true },
                displayMode = displayMode,
                onDisplayModeChange = onDisplayModeChange
            )

            period?.let { activePeriod ->
                GalleryActivePeriod(
                    label = activePeriod.label(),
                    onClear = { period = null }
                )
            }

            if (isSelectionMode) {
                SelectionHeader(
                    count = visibleSelection.size,
                    onCancel = { selectedEventIds = emptySet() }
                )
            }

            when {
                events.isEmpty() -> {
                    EmptyGalleryState(
                        icon = Icons.Outlined.PhotoCamera,
                        title = "Votre galerie est vide",
                        description = "Les événements détectés apparaîtront ici."
                    )
                }

                filteredEvents.isEmpty() -> {
                    EmptyGalleryState(
                        icon = Icons.Outlined.Search,
                        title = "Aucun résultat",
                        description = "Aucun événement ne correspond à vos filtres.",
                        actionLabel = "Effacer les filtres",
                        onAction = ::clearFilters
                    )
                }

                displayMode == GalleryDisplayMode.GRID -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        dateGroups.forEach { group ->
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                DateGroupTitle(group.date)
                            }

                            items(
                                items = group.events,
                                key = { event -> event.id }
                            ) { event ->
                                GalleryEventItem(
                                    event = event,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = event.id in visibleSelection,
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleSelection(event.id)
                                        } else {
                                            onEventClick(event)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            selectedEventIds = selectedEventIds + event.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                displayMode == GalleryDisplayMode.LIST -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        dateGroups.forEach { group ->
                            item {
                                DateGroupTitle(group.date)
                            }

                            lazyListItems(
                                items = group.events,
                                key = { event -> event.id }
                            ) { event ->
                                GalleryEventRow(
                                    event = event,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = event.id in visibleSelection,
                                    onClick = {
                                        if (isSelectionMode) {
                                            toggleSelection(event.id)
                                        } else {
                                            onEventClick(event)
                                        }
                                    },
                                    onLongClick = {
                                        if (!isSelectionMode) {
                                            selectedEventIds = selectedEventIds + event.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isSelectionMode) {
            SelectionActionBar(
                onSelectAll = {
                    selectedEventIds = selectedEventIds + filteredEvents.map { it.id }
                },
                onDelete = { showDeleteConfirmation = true }
            )
        }
    }

    if (showPeriodPicker) {
        PeriodPickerDialog(
            initialPeriod = period,
            onDismiss = { showPeriodPicker = false },
            onConfirm = { selected ->
                period = selected
                showPeriodPicker = false
            }
        )
    }

    if (showDeleteConfirmation && isSelectionMode) {
        DeleteEventsDialog(
            count = visibleSelection.size,
            onConfirm = {
                onDeleteEvents(visibleSelection.toList())
                selectedEventIds = emptySet()
                showDeleteConfirmation = false
            },
            onDismiss = { showDeleteConfirmation = false }
        )
    }
}

@Composable
private fun DateGroupTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 8.dp),
        style = IntruderTypography.TitleSmall,
        color = IntruderColors.TextPrimary
    )
}

@Composable
private fun SelectionHeader(
    count: Int,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quitter la sélection",
                modifier = Modifier.size(20.dp),
                tint = IntruderColors.Primary
            )
        }

        Text(
            text = if (count > 1) "$count sélectionnés" else "$count sélectionné",
            style = IntruderTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = IntruderColors.TextPrimary
        )
    }
}

// Sits above the bottom navigation, as in the selection mockup.
@Composable
private fun SelectionActionBar(
    onSelectAll: () -> Unit,
    onDelete: () -> Unit
) {
    val buttonShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntruderColors.Surface)
    ) {
        HorizontalDivider(color = IntruderColors.Border)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onSelectAll,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntruderColors.SurfaceVariant,
                    contentColor = IntruderColors.PrimaryVariant
                )
            ) {
                Text(
                    text = "Tout sélectionner",
                    style = IntruderTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Button(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                shape = buttonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = IntruderColors.Error,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Supprimer",
                    style = IntruderTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

@Composable
private fun EmptyGalleryState(
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = IntruderColors.Primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = IntruderTypography.TitleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = IntruderColors.TextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = description,
            style = IntruderTypography.Body,
            color = IntruderColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null) {
            TextButton(
                onClick = onAction,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = IntruderColors.Primary
                )
            ) {
                Text(actionLabel)
            }
        }
    }
}
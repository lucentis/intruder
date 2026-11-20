package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyListItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.domain.model.Event

// A Set cannot be put in the saved state directly: it is converted to a list of Longs.
private val SelectedIdsSaver = listSaver<Set<Long>, Long>(
    save = { it.toList() },
    restore = { it.toSet() }
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

    // Saved across configuration changes (rotation, dark mode) and process death, like
    // the confirmation flag below, so the two never get out of sync.
    var selectedEventIds by rememberSaveable(stateSaver = SelectedIdsSaver) {
        mutableStateOf(emptySet<Long>())
    }

    var showDeleteConfirmation by rememberSaveable {
        mutableStateOf(false)
    }

    // Recomputed only when the list or the filter changes, not at every recomposition.
    val filteredEvents = remember(events, selectedFilter) {
        events.filterByGalleryFilter(selectedFilter)
    }
    val dateGroups = remember(filteredEvents) { filteredEvents.groupByDate() }
    val isSelectionMode = selectedEventIds.isNotEmpty()

    fun toggleSelection(eventId: Long) {
        selectedEventIds = if (eventId in selectedEventIds) {
            selectedEventIds - eventId
        } else {
            selectedEventIds + eventId
        }
    }

    fun selectEvent(eventId: Long) {
        selectedEventIds = selectedEventIds + eventId
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        GalleryHeader()

        if (isSelectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedEventIds.size} sélectionné${if (selectedEventIds.size > 1) "s" else ""}",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            selectedEventIds = selectedEventIds +
                                filteredEvents.map { it.id }
                        }
                    ) {
                        Text("Tout sélectionner")
                    }

                    TextButton(
                        onClick = {
                            selectedEventIds = emptySet()
                        }
                    ) {
                        Text("Terminer")
                    }

                    TextButton(
                        onClick = {
                            showDeleteConfirmation = true
                        }
                    ) {
                        Text("Supprimer")
                    }
                }
            }
        }

        if (!isSelectionMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == GalleryFilter.ALL,
                    onClick = {
                        selectedFilter = GalleryFilter.ALL
                    },
                    label = { Text("Tous") }
                )

                FilterChip(
                    selected = selectedFilter == GalleryFilter.SUCCESS,
                    onClick = {
                        selectedFilter = GalleryFilter.SUCCESS
                    },
                    label = { Text("Succès") }
                )

                FilterChip(
                    selected = selectedFilter == GalleryFilter.FAILED,
                    onClick = {
                        selectedFilter = GalleryFilter.FAILED
                    },
                    label = { Text("Échecs") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilterChip(
                    selected = displayMode == GalleryDisplayMode.GRID,
                    onClick = {
                        onDisplayModeChange(GalleryDisplayMode.GRID)
                    },
                    label = { Text("Grille") }
                )

                FilterChip(
                    selected = displayMode == GalleryDisplayMode.LIST,
                    onClick = {
                        onDisplayModeChange(GalleryDisplayMode.LIST)
                    },
                    label = { Text("Liste") }
                )
            }
        }

        when {
            events.isEmpty() -> {
                EmptyGalleryState(
                    title = "Votre galerie est vide",
                    description = "Les événements détectés apparaîtront ici."
                )
            }

            filteredEvents.isEmpty() -> {
                EmptyGalleryState(
                    title = "Aucun résultat",
                    description = "Aucun événement ne correspond à vos filtres.",
                    action = {
                        selectedFilter = GalleryFilter.ALL
                    }
                )
            }

            displayMode == GalleryDisplayMode.GRID -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateGroups.forEach { group ->
                        item(
                            span = {
                                GridItemSpan(maxLineSpan)
                            }
                        ) {
                            Text(
                                text = group.date,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    bottom = 4.dp
                                )
                            )
                        }

                        items(
                            items = group.events,
                            key = { event -> event.id }
                        ) { event ->
                            GalleryEventItem(
                                event = event,
                                isSelected = event.id in selectedEventIds,
                                onClick = {
                                    if (isSelectionMode) {
                                        toggleSelection(event.id)
                                    } else {
                                        onEventClick(event)
                                    }
                                },
                                onLongClick = {
                                    selectEvent(event.id)
                                }
                            )
                        }
                    }
                }
            }

            displayMode == GalleryDisplayMode.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    dateGroups.forEach { group ->
                        item {
                            Text(
                                text = group.date,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    bottom = 4.dp
                                )
                            )
                        }

                        lazyListItems(
                            items = group.events,
                            key = { event -> event.id }
                        ) { event ->
                            GalleryEventRow(
                                event = event,
                                isSelected = event.id in selectedEventIds,
                                onClick = {
                                    if (isSelectionMode) {
                                        toggleSelection(event.id)
                                    } else {
                                        onEventClick(event)
                                    }
                                },
                                onLongClick = {
                                    selectEvent(event.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation && selectedEventIds.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
            },
            title = {
                Text("Supprimer les événements ?")
            },
            text = {
                Text(
                    "Voulez-vous supprimer définitivement " +
                        "${selectedEventIds.size} événement(s) " +
                        "ainsi que les photos associées ?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteEvents(selectedEventIds.toList())
                        selectedEventIds = emptySet()
                        showDeleteConfirmation = false
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

@Composable
private fun EmptyGalleryState(
    title: String,
    description: String,
    action: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (action != null) {
            TextButton(onClick = action) {
                Text("Effacer les filtres")
            }
        }
    }
}
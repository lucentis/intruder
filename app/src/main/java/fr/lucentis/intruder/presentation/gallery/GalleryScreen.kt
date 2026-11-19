
package fr.lucentis.intruder.presentation.gallery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.lucentis.intruder.domain.gallery.GalleryDisplayMode
import fr.lucentis.intruder.domain.model.Event

@Composable
fun GalleryScreen(
    events: List<Event>,
    displayMode: GalleryDisplayMode,
    onDisplayModeChange: (GalleryDisplayMode) -> Unit
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(GalleryFilter.ALL)
    }

    val filteredEvents = events.filterByGalleryFilter(selectedFilter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Intruder",
            style = MaterialTheme.typography.headlineMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == GalleryFilter.ALL,
                onClick = { selectedFilter = GalleryFilter.ALL },
                label = { Text("Tous") }
            )

            FilterChip(
                selected = selectedFilter == GalleryFilter.SUCCESS,
                onClick = { selectedFilter = GalleryFilter.SUCCESS },
                label = { Text("Succès") }
            )

            FilterChip(
                selected = selectedFilter == GalleryFilter.FAILED,
                onClick = { selectedFilter = GalleryFilter.FAILED },
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
                val dateGroups = filteredEvents.groupByDate()

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dateGroups.forEach { group ->
                        item(
                            span = {
                                androidx.compose.foundation.lazy.grid.GridItemSpan(
                                    maxLineSpan
                                )
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
                            GalleryEventItem(event = event)
                        }
                    }
                }
            }
        }
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


package com.example.wildlife_app.ui.sightinglist

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.ui.components.SightingListItem
import kotlin.math.abs
import androidx.compose.foundation.layout.padding

@Composable
fun SightingListScreen(
    viewModel: SightingListViewModel,
    onAddSighting: () -> Unit,
    onSightingClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferCommonName by viewModel.preferCommonName.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val deletedSighting by viewModel.deletedSighting.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var sightingToDelete by remember {
        mutableStateOf<SightingWithSpecies?>(null)
    }

    var sortMenuExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(deletedSighting?.sighting?.id) {
        val deleted = deletedSighting

        if (deleted != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Sighting deleted",
                actionLabel = "Undo",
                withDismissAction = true
            )

            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "My Wildlife Sightings",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search sightings") },
                placeholder = {
                    Text("Species, notes or coordinates")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        sortMenuExpanded = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = when (sortOrder) {
                            SightingSortOrder.NEWEST_FIRST ->
                                "Sort: Newest first"

                            SightingSortOrder.OLDEST_FIRST ->
                                "Sort: Oldest first"
                        }
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = {
                        sortMenuExpanded = false
                    }
                ) {
                    DropdownMenuItem(
                        text = { Text("Newest first") },
                        onClick = {
                            viewModel.onSortOrderChanged(
                                SightingSortOrder.NEWEST_FIRST
                            )
                            sortMenuExpanded = false
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Oldest first") },
                        onClick = {
                            viewModel.onSortOrderChanged(
                                SightingSortOrder.OLDEST_FIRST
                            )
                            sortMenuExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onAddSighting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Sighting")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onSettingsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = uiState) {

                SightingListUiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Loading sightings...")
                    }
                }

                SightingListUiState.Empty -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isBlank()) {
                                "No wildlife sightings yet."
                            } else {
                                "No sightings match your search."
                            },
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Add your first sighting to get started."
                            } else {
                                "Try another species name, note or coordinate."
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    viewModel.onSearchQueryChanged("")
                                }
                            ) {
                                Text("Clear search")
                            }
                        }
                    }
                }

                is SightingListUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is SightingListUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.sightings,
                            key = { it.sighting.id }
                        ) { sighting ->

                            val displayName = if (preferCommonName) {
                                sighting.species.commonName
                                    ?.takeIf { it.isNotBlank() }
                                    ?: sighting.species.scientificName
                            } else {
                                sighting.species.scientificName
                            }

                            var dragOffset by remember(
                                sighting.sighting.id
                            ) {
                                mutableStateOf(0f)
                            }

                            val swipeThreshold = with(
                                LocalDensity.current
                            ) {
                                120.dp.toPx()
                            }

                            Column(
                                verticalArrangement =
                                    Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            translationX = dragOffset
                                        }
                                        .pointerInput(
                                            sighting.sighting.id,
                                            swipeThreshold
                                        ) {
                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = {
                                                        change,
                                                        dragAmount ->
                                                    change.consume()

                                                    dragOffset =
                                                        (dragOffset + dragAmount)
                                                            .coerceIn(
                                                                -swipeThreshold,
                                                                swipeThreshold
                                                            )
                                                },
                                                onDragEnd = {
                                                    if (
                                                        abs(dragOffset) >=
                                                        swipeThreshold
                                                    ) {
                                                        sightingToDelete =
                                                            sighting
                                                    }

                                                    dragOffset = 0f
                                                },
                                                onDragCancel = {
                                                    dragOffset = 0f
                                                }
                                            )
                                        }
                                ) {
                                    SightingListItem(
                                        title = displayName,
                                        subtitle = if (preferCommonName) {
                                            sighting.species.scientificName
                                        } else {
                                            sighting.species.commonName
                                                ?.takeIf { it.isNotBlank() }
                                        },
                                        supportingText =
                                            "Location: " +
                                                    "${sighting.sighting.latitude}, " +
                                                    "${sighting.sighting.longitude}",
                                        photoUri = sighting.sighting.photoUri,
                                        onClick = {
                                            onSightingClick(
                                                sighting.sighting.id
                                            )
                                        }
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement =
                                        Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            sightingToDelete = sighting
                                        }
                                    ) {
                                        Text("Delete sighting")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    sightingToDelete?.let { sighting ->
        AlertDialog(
            onDismissRequest = {
                sightingToDelete = null
            },
            title = {
                Text("Delete sighting?")
            },
            text = {
                Text(
                    "This sighting will be removed from your records."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSighting(sighting)
                        sightingToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        sightingToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

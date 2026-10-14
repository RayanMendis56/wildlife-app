
package com.example.wildlife_app.ui.sightinglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.data.local.relation.SightingWithSpecies

@Composable
fun SightingListScreen(
    viewModel: SightingListViewModel,
    onAddSighting: () -> Unit,
    onSightingClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferCommonName by viewModel.preferCommonName.collectAsState()

    var sightingToDelete by remember {
        mutableStateOf<SightingWithSpecies?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Wildlife Sightings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

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
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text("Loading sightings...")
                }
            }

            SightingListUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No wildlife sightings yet.")
                    Text("Add your first sighting to get started.")
                }
            }

            is SightingListUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
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
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.sightings,
                        key = { it.sighting.id }
                    ) { sighting ->
                        Card(
                            onClick = {
                                onSightingClick(sighting.sighting.id)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = if (preferCommonName) {
                                        sighting.species.commonName
                                            ?.takeIf { it.isNotBlank() }
                                            ?: sighting.species.scientificName
                                    } else {
                                        sighting.species.scientificName
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = sighting.species.scientificName,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Location: " +
                                            "${sighting.sighting.latitude}, " +
                                            "${sighting.sighting.longitude}"
                                )

                                sighting.sighting.notes
                                    ?.takeIf { it.isNotBlank() }
                                    ?.let { notes ->
                                        Text(text = "Notes: $notes")
                                    }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            sightingToDelete = sighting
                                        }
                                    ) {
                                        Text("Delete")
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

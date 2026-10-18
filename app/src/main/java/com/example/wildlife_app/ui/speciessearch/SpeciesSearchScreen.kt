package com.example.wildlife_app.ui.speciessearch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.ui.addedit.AddEditSightingViewModel

@Composable
fun SpeciesSearchScreen(
    viewModel: AddEditSightingViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "1. Species",
            style = MaterialTheme.typography.titleLarge
        )

        OutlinedTextField(
            value = uiState.speciesQuery,
            onValueChange = viewModel::onSpeciesQueryChanged,
            label = { Text("Search wildlife species") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = viewModel::searchSpecies,
            enabled = uiState.speciesQuery.isNotBlank() &&
                    !uiState.isSearchingSpecies,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSearchingSpecies) {
                CircularProgressIndicator()
            } else {
                Text("Search Species")
            }
        }

        uiState.species?.let { species ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Selected species: " +
                            (
                                    species.commonName
                                        ?.takeIf { it.isNotBlank() }
                                        ?: species.scientificName
                                    ),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = species.scientificName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.speciesResults.forEach { species ->
            Card(
                onClick = {
                    viewModel.onSpeciesSelected(species)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = species.commonName
                            ?.takeIf { it.isNotBlank() }
                            ?: species.scientificName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = species.scientificName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (
            uiState.hasSearchedSpecies &&
            uiState.speciesResults.isEmpty() &&
            uiState.speciesSearchError == null
        ) {
            Text(
                text = "No species found. Try another name.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        uiState.speciesSearchError?.let { message ->
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )

                Button(
                    onClick = { viewModel.searchSpecies() },
                    enabled = !uiState.isSearchingSpecies
                ) {
                    Text("Retry")
                }
            }
        }
    }
}
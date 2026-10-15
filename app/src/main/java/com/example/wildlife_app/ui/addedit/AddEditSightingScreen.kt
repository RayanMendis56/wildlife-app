
package com.example.wildlife_app.ui.addedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.ui.components.PhotoPicker

@Composable
fun AddEditSightingScreen(
    viewModel: AddEditSightingViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedSightingId) {
        if (uiState.savedSightingId != null) {
            onSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Wildlife Sighting",
            style = MaterialTheme.typography.headlineSmall
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
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = "Searching for species"
                    }
                )
            } else {
                Text("Search Species")
            }
        }

        if (uiState.isSearchingSpecies) {
            Text("Searching for wildlife species...")
        }

        uiState.species?.let { species ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Selected species: " +
                                    "${species.commonName ?: species.scientificName}. " +
                                    "Scientific name: ${species.scientificName}"
                    }
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Selected species: " +
                            (species.commonName
                                ?.takeIf { it.isNotBlank() }
                                ?: species.scientificName),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription =
                            "Select species: " +
                                    "${species.commonName ?: species.scientificName}, " +
                                    "scientific name ${species.scientificName}"
                    },
                colors = CardDefaults.cardColors()
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

        OutlinedTextField(
            value = uiState.latitude,
            onValueChange = viewModel::onLatitudeChanged,
            label = { Text("Latitude") },
            supportingText = { Text("e.g. -1.2833 or 1° 17' S") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.longitude,
            onValueChange = viewModel::onLongitudeChanged,
            label = { Text("Longitude") },
            supportingText = { Text("e.g. 36.8167 or 36° 49' E") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::onNotesChanged,
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        // Select, replace, or remove the sighting photo.
        PhotoPicker(
            selectedPhotoUri = uiState.photoUri,
            onGalleryPhotoPicked = viewModel::onGalleryPhotoPicked,
            prepareCameraPhoto = viewModel::prepareCameraPhoto,
            onCameraResult = viewModel::onCameraResult,
            onCameraUnavailable = viewModel::onCameraUnavailable,
            onRemovePhoto = { viewModel.onPhotoSelected(null) }
        )

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Saving wildlife sighting"
                        }
                    )
                } else {
                    Text("Save Sighting")
                }
            }
        }
    }
}


package com.example.wildlife_app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SightingDetailScreen(
    sightingId: Long,
    viewModel: SightingDetailViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sightingId) {
        viewModel.loadSighting(sightingId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }

        when (val state = uiState) {
            SightingDetailUiState.Loading -> {
                CircularProgressIndicator()
                Text("Loading sighting...")
            }

            is SightingDetailUiState.Success -> {
                val item = state.sighting
                val sighting = item.sighting
                val species = item.species

                Text(
                    text = species.commonName ?: species.scientificName,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = species.scientificName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Latitude: ${sighting.latitude}")
                Text("Longitude: ${sighting.longitude}")
                Text(
                    text = "Date recorded: ${
                        java.text.SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            java.util.Locale.getDefault()
                        ).format(java.util.Date(sighting.timestamp))
                    }"
                )
                Text("Notes: ${sighting.notes?.takeIf { it.isNotBlank() } ?: "No notes"}")
            }

            SightingDetailUiState.NotFound -> {
                Text("Sighting not found.")
            }

            is SightingDetailUiState.Error -> {
                Text("Error: ${state.message}")
            }
        }
    }
}

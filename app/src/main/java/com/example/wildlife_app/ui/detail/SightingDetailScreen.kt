
package com.example.wildlife_app.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun SightingDetailScreen(
    sightingId: Long,
    viewModel: SightingDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(sightingId) {
        viewModel.loadSighting(sightingId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                val displayName = species.commonName ?: species.scientificName

                sighting.photoUri?.let { photo ->
                    AsyncImage(
                        model = photo,
                        contentDescription = "Photo of $displayName from this sighting",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = displayName,
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

                Button(
                    onClick = {
                        val opened = openInMaps(
                            context,
                            sighting.latitude,
                            sighting.longitude,
                            displayName
                        )
                        if (!opened) {
                            Toast.makeText(
                                context,
                                "No maps app or browser found on this device.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open location in Maps")
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit sighting")
                }
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

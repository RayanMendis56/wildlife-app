
package com.example.wildlife_app.ui.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SightingDetailScreen(
    sightingId: Long,
    viewModel: SightingDetailViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDeleting by viewModel.isDeleting.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()
    val context = LocalContext.current

    var showDeleteConfirmation by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(sightingId) {
        viewModel.loadSighting(sightingId)
    }

    LaunchedEffect(deleteResult) {
        when (deleteResult) {
            "SUCCESS" -> {
                viewModel.clearDeleteResult()
                onBack()
            }

            null -> Unit

            else -> {
                Toast.makeText(
                    context,
                    deleteResult,
                    Toast.LENGTH_LONG
                ).show()
                viewModel.clearDeleteResult()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }

        when (val state = uiState) {
            SightingDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator()
                        Text("Loading sighting...")
                    }
                }
            }

            is SightingDetailUiState.Success -> {
                val item = state.sighting
                val sighting = item.sighting
                val species = item.species

                val displayName = species.commonName
                    ?.takeIf { it.isNotBlank() }
                    ?: species.scientificName

                val recordedDate = rememberFormattedDate(
                    sighting.timestamp
                )

                if (!sighting.photoUri.isNullOrBlank()) {
                    AsyncImage(
                        model = sighting.photoUri,
                        contentDescription = "Photo of $displayName",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = Icons.Outlined.Pets,
                                contentDescription = null,
                                modifier = Modifier.height(64.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            Text(
                                text = "No photograph available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = species.scientificName,
                        style = MaterialTheme.typography.titleMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Wildlife observation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                DetailInfoCard(
                    title = "Observation date and time",
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null
                        )
                    }
                ) {
                    Text(
                        text = recordedDate,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                DetailInfoCard(
                    title = "Location",
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null
                        )
                    }
                ) {
                    Text("Latitude: ${sighting.latitude}")
                    Text("Longitude: ${sighting.longitude}")

                    Spacer(Modifier.height(4.dp))

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
                }

                DetailInfoCard(
                    title = "Observation notes",
                    icon = {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Outlined.Notes,
                            contentDescription = null
                        )
                    }
                ) {
                    val notes = sighting.notes
                        ?.takeIf { it.isNotBlank() }
                        ?: "No notes have been added."

                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                HorizontalDivider()

                Button(
                    onClick = onEdit,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit sighting")
                }

                OutlinedButton(
                    onClick = {
                        showDeleteConfirmation = true
                    },
                    enabled = !isDeleting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = null
                    )

                    Spacer(Modifier.height(0.dp))

                    Text("Delete sighting")
                }

                if (isDeleting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(24.dp)
                        )

                        Spacer(Modifier.height(0.dp))

                        Text("Deleting sighting...")
                    }
                }

                if (showDeleteConfirmation) {
                    AlertDialog(
                        onDismissRequest = {
                            if (!isDeleting) {
                                showDeleteConfirmation = false
                            }
                        },
                        title = {
                            Text("Delete sighting?")
                        },
                        text = {
                            Text(
                                "Are you sure you want to delete " +
                                        "\"$displayName\"? This action cannot be undone."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation = false
                                    viewModel.deleteSighting(item)
                                },
                                enabled = !isDeleting
                            ) {
                                Text("Delete")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteConfirmation = false
                                },
                                enabled = !isDeleting
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            SightingDetailUiState.NotFound -> {
                Text(
                    text = "Sighting not found.",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            is SightingDetailUiState.Error -> {
                Text(
                    text = "Error: ${state.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                icon()

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            content()
        }
    }
}

@Composable
private fun rememberFormattedDate(timestamp: Long): String {
    return remember(timestamp) {
        SimpleDateFormat(
            "dd MMMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}

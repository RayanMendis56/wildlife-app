
package com.example.wildlife_app.ui.outings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.data.local.entity.OutingEntity
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun OutingDetailScreen(
    viewModel: OutingDetailViewModel,
    onBack: () -> Unit,
    onSightingClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Outing Details",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        operationMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        when (val state = uiState) {
            OutingDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is OutingDetailUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is OutingDetailUiState.Success -> {
                Button(
                    onClick = {
                        viewModel.clearOperationMessage()
                        showEditDialog = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Outing")
                }

                Button(
                    onClick = {
                        viewModel.clearOperationMessage()
                        showDeleteDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete Outing")
                }

                OutingSummaryCard(
                    outing = state.outing,
                    sightingCount = state.sightings.size
                )

                Text(
                    text = "Wildlife Sightings",
                    style = MaterialTheme.typography.titleLarge
                )

                if (state.sightings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Pets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "No sightings recorded",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = "Record a wildlife sighting and assign it to this outing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.sightings,
                            key = { it.sighting.id }
                        ) { sighting ->
                            OutingSightingCard(
                                sighting = sighting,
                                onClick = {
                                    onSightingClick(sighting.sighting.id)
                                }
                            )
                        }
                    }
                }

                if (showEditDialog) {
                    EditOutingDialog(
                        outing = state.outing,
                        onDismiss = {
                            showEditDialog = false
                        },
                        onSave = { name, date ->
                            viewModel.updateOuting(
                                name = name,
                                date = date,
                                onSaved = {
                                    showEditDialog = false
                                }
                            )
                        }
                    )
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showDeleteDialog = false
                        },
                        title = {
                            Text("Delete Outing?")
                        },
                        text = {
                            Text(
                                "Are you sure you want to delete " +
                                        "\"${state.outing.name}\"? " +
                                        "This action cannot be undone. " +
                                        "Wildlife sightings will be kept, " +
                                        "but will no longer be linked to this outing."
                            )
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false

                                    viewModel.deleteOuting(
                                        onDeleted = onBack
                                    )
                                }
                            ) {
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showDeleteDialog = false
                                }
                            ) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OutingSummaryCard(
    outing: OutingEntity,
    sightingCount: Int
) {
    val dateText = SimpleDateFormat(
        "dd MMM yyyy",
        LocalLocale.current.platformLocale
    ).format(Date(outing.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = outing.name,
                style = MaterialTheme.typography.headlineSmall
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Outing date",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(text = dateText)
            }

            Text(
                text = "$sightingCount ${
                    if (sightingCount == 1) "sighting" else "sightings"
                }",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun EditOutingDialog(
    outing: OutingEntity,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var name by remember(outing.id) {
        mutableStateOf(outing.name)
    }

    var selectedDate by remember(outing.id) {
        mutableLongStateOf(outing.date)
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val dateText = remember(selectedDate) {
        SimpleDateFormat(
            "dd MMM yyyy",
            Locale.getDefault()
        ).format(Date(selectedDate))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Outing")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = {
                        Text("Outing name")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Outing date")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                showDatePicker = true
                            }
                        ) {
                            Text("Change")
                        }
                    }
                )

                validationError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Please enter an outing name."
                    } else {
                        onSave(name.trim(), selectedDate)
                    }
                }
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun OutingSightingCard(
    sighting: SightingWithSpecies,
    onClick: () -> Unit
) {
    val timestampText = SimpleDateFormat(
        "dd MMM yyyy, HH:mm",
        LocalLocale.current.platformLocale
    ).format(Date(sighting.sighting.timestamp))

    val commonName = sighting.species.commonName
        ?: "Unknown species"

    val scientificName = sighting.species.scientificName
        ?: "Scientific name unavailable"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = commonName,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = scientificName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Observation date",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = timestampText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Observation coordinates",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format(
                        Locale.US,
                        "%.5f, %.5f",
                        sighting.sighting.latitude,
                        sighting.sighting.longitude
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "View sighting details",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

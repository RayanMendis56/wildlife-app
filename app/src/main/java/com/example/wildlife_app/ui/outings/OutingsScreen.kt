
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
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.data.local.entity.OutingEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalLocale

@Composable
fun OutingsScreen(
    viewModel: OutingsViewModel,
    onBack: () -> Unit,
    onOutingClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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

            Spacer(Modifier.width(8.dp))

            Text(
                text = "My Outings",
                style = MaterialTheme.typography.headlineMedium
            )
        }

        Button(
            onClick = {
                errorMessage = null
                showAddDialog = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Outing")
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        when (val state = uiState) {
            OutingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is OutingsUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is OutingsUiState.Success -> {
                if (state.outings.isEmpty()) {
                    OutingsEmptyState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.outings,
                            key = { it.id }
                        ) { outing ->
                            OutingCard(outing) {
                                onOutingClick(outing.id)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddOutingDialog(
            onDismiss = {
                showAddDialog = false
            },
            onSave = { name, date ->
                viewModel.addOuting(
                    name = name,
                    date = date,
                    onSaved = {
                        showAddDialog = false
                        errorMessage = null
                    },
                    onError = { message ->
                        errorMessage = message
                    }
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddOutingDialog(
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedDate by remember {
        mutableLongStateOf(System.currentTimeMillis())
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
            Text("Create Outing")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    placeholder = {
                        Text("e.g. Horton Plains Birdwatching")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationError != null
                )

                Text(
                    text = "Outing date: $dateText",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = dateText,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Date")
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                showDatePicker = true
                            }
                        ) {
                            Text("Choose")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
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
                        validationError =
                            "Please enter an outing name."
                    } else {
                        onSave(name.trim(), selectedDate)
                    }
                }
            ) {
                Text("Save")
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
private fun OutingsEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Outlined.Explore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "No outings yet",
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "Your wildlife outings will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OutingCard(
    outing: OutingEntity,
    onClick: () -> Unit
) {
    val dateText = SimpleDateFormat(
        "dd MMM yyyy",
        LocalLocale.current.platformLocale
    ).format(Date(outing.date))

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
                text = outing.name,
                style = MaterialTheme.typography.titleLarge
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

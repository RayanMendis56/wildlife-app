
package com.example.wildlife_app.ui.addedit

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.wildlife_app.domain.model.Species
import com.example.wildlife_app.ui.components.PhotoPicker
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.DateFormat
import java.util.Date
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSightingScreen(
    viewModel: AddEditSightingViewModel,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var outingMenuExpanded by remember { mutableStateOf(false) }
    var isGettingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }

    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun fetchCurrentLocation() {
        val hasFinePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val hasCoarsePermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) {
            locationError = "Location permission has not been granted."
            return
        }

        isGettingLocation = true
        locationError = null

        try {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L
            )
                .setMaxUpdates(1)
                .build()

            val callback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)

                    val location: Location? = result.lastLocation

                    isGettingLocation = false

                    if (location != null) {
                        viewModel.onLatitudeChanged(
                            location.latitude.toString()
                        )
                        viewModel.onLongitudeChanged(
                            location.longitude.toString()
                        )
                        locationError = null
                    } else {
                        locationError =
                            "Unable to obtain your location. " +
                                    "Check that device location is enabled and try again."
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                request,
                callback,
                Looper.getMainLooper()
            ).addOnFailureListener { exception ->
                fusedLocationClient.removeLocationUpdates(callback)
                isGettingLocation = false
                locationError =
                    exception.localizedMessage
                        ?: "Unable to retrieve your location."
            }
        } catch (exception: SecurityException) {
            isGettingLocation = false
            locationError =
                "Location permission is required. Please grant permission and try again."
        } catch (exception: Exception) {
            isGettingLocation = false
            locationError =
                exception.localizedMessage
                    ?: "An error occurred while retrieving your location."
        }
    }

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (granted) {
                fetchCurrentLocation()
            } else {
                locationError =
                    "Location permission denied. You can still enter coordinates manually."
            }
        }

    LaunchedEffect(uiState.savedSightingId) {
        if (uiState.savedSightingId != null) {
            onSaved()
        }
    }

    val selectedOuting = uiState.outings.firstOrNull {
        it.id == uiState.outingId
    }

    val dateText = remember(uiState.timestamp) {
        DateFormat.getDateInstance(DateFormat.MEDIUM)
            .format(Date(uiState.timestamp))
    }

    val timeText = remember(uiState.timestamp) {
        DateFormat.getTimeInstance(DateFormat.SHORT)
            .format(Date(uiState.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Wildlife Sighting",
            style = MaterialTheme.typography.headlineSmall
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "1. Species",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                            uiState.searchState !is SpeciesSearchState.Loading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search Species")
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

                SpeciesSearchResults(
                    state = uiState.searchState,
                    onSelect = { species ->
                        viewModel.onSpeciesSelected(species)
                        focusManager.clearFocus()
                    },
                    onRetry = viewModel::searchSpecies
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. Photo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                PhotoPicker(
                    selectedPhotoUri = uiState.photoUri,
                    onGalleryPhotoPicked = viewModel::onGalleryPhotoPicked,
                    prepareCameraPhoto = viewModel::prepareCameraPhoto,
                    onCameraResult = viewModel::onCameraResult,
                    onCameraUnavailable = viewModel::onCameraUnavailable,
                    onRemovePhoto = {
                        viewModel.onPhotoSelected(null)
                    }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "3. Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Button(
                    onClick = {
                        val hasFinePermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        val hasCoarsePermission =
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED

                        if (hasFinePermission || hasCoarsePermission) {
                            fetchCurrentLocation()
                        } else {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !isGettingLocation,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isGettingLocation) {
                        CircularProgressIndicator()
                    } else {
                        Text("Use My Current Location")
                    }
                }

                if (isGettingLocation) {
                    Text("Getting your location...")
                }

                locationError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                OutlinedTextField(
                    value = uiState.latitude,
                    onValueChange = viewModel::onLatitudeChanged,
                    label = { Text("Latitude") },
                    supportingText = {
                        Text("Enter a value between -90 and 90.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.longitude,
                    onValueChange = viewModel::onLongitudeChanged,
                    label = { Text("Longitude") },
                    supportingText = {
                        Text("Enter a value between -180 and 180.")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "4. Date and Time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text("Observation date: $dateText")

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = uiState.timestamp
                        }

                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val updatedCalendar =
                                    Calendar.getInstance().apply {
                                        timeInMillis = uiState.timestamp
                                        set(Calendar.YEAR, year)
                                        set(Calendar.MONTH, month)
                                        set(Calendar.DAY_OF_MONTH, day)
                                    }

                                viewModel.onTimestampChanged(
                                    updatedCalendar.timeInMillis
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Date")
                }

                Text("Observation time: $timeText")

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance().apply {
                            timeInMillis = uiState.timestamp
                        }

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val updatedCalendar =
                                    Calendar.getInstance().apply {
                                        timeInMillis = uiState.timestamp
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, minute)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }

                                viewModel.onTimestampChanged(
                                    updatedCalendar.timeInMillis
                                )
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            android.text.format.DateFormat.is24HourFormat(context)
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Choose Time")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "5. Outing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = outingMenuExpanded,
                    onExpandedChange = {
                        outingMenuExpanded = !outingMenuExpanded
                    }
                ) {
                    OutlinedTextField(
                        value = selectedOuting?.name ?: "No outing selected",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select outing (optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = outingMenuExpanded
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = outingMenuExpanded,
                        onDismissRequest = {
                            outingMenuExpanded = false
                        }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No outing") },
                            onClick = {
                                viewModel.onOutingSelected(null)
                                outingMenuExpanded = false
                            }
                        )

                        uiState.outings.forEach { outing ->
                            DropdownMenuItem(
                                text = { Text(outing.name) },
                                onClick = {
                                    viewModel.onOutingSelected(outing.id)
                                    outingMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                if (uiState.outings.isEmpty()) {
                    Text(
                        text = "No outings available yet. You can save this " +
                                "sighting without an outing.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "6. Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChanged,
                    label = { Text("Observation notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        }

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
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving
            ) {
                Text("Cancel")
            }

            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Save Sighting")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SpeciesSearchResults(
    state: SpeciesSearchState,
    onSelect: (Species) -> Unit,
    onRetry: () -> Unit
) {
    when (state) {
        SpeciesSearchState.Idle -> Unit

        SpeciesSearchState.Loading -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .semantics { contentDescription = "Searching for species" }
                )
                Text("Searching for species…")
            }
        }

        SpeciesSearchState.Empty -> {
            Text(
                text = "No matching species found. Try a different name, such as a common bird name.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        is SpeciesSearchState.Error -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
                OutlinedButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }

        is SpeciesSearchState.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.results.forEach { species ->
                    Card(
                        onClick = { onSelect(species) },
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
            }
        }
    }
}

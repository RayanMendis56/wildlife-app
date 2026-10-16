
package com.example.wildlife_app.ui.addedit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.local.PhotoStorage
import com.example.wildlife_app.data.local.entity.SightingEntity
import com.example.wildlife_app.data.repository.SightingRepository
import com.example.wildlife_app.data.repository.SpeciesRepository
import com.example.wildlife_app.domain.CoordinateParser
import com.example.wildlife_app.domain.model.Species
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException

data class AddEditSightingUiState(
    val species: Species? = null,
    val latitude: String = "",
    val longitude: String = "",
    val notes: String = "",
    val photoUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSightingId: Long? = null,
    val speciesQuery: String = "",
    val speciesResults: List<Species> = emptyList(),
    val isSearchingSpecies: Boolean = false
)

class AddEditSightingViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val sightingRepository: SightingRepository,
    private val speciesRepository: SpeciesRepository,
    private val photoStorage: PhotoStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AddEditSightingUiState(
            latitude = savedStateHandle["latitude"] ?: "",
            longitude = savedStateHandle["longitude"] ?: "",
            notes = savedStateHandle["notes"] ?: "",
            photoUri = savedStateHandle["photoUri"],
            timestamp = savedStateHandle["timestamp"]
                ?: System.currentTimeMillis(),
            speciesQuery = savedStateHandle["speciesQuery"] ?: "",
            species = restoreSelectedSpecies()
        )
    )

    val uiState: StateFlow<AddEditSightingUiState> =
        _uiState.asStateFlow()

    private var editingSightingId: Long? = null

    private fun restoreSelectedSpecies(): Species? {
        val id = savedStateHandle.get<Long>("selectedSpeciesId")
            ?: return null

        val scientificName =
            savedStateHandle.get<String>("selectedSpeciesScientificName")
                ?: return null

        return Species(
            id = id,
            scientificName = scientificName,
            commonName = savedStateHandle["selectedSpeciesCommonName"],
            photoUrl = savedStateHandle["selectedSpeciesPhotoUrl"] ?: ""
        )
    }

    fun onSpeciesQueryChanged(query: String) {
        savedStateHandle["speciesQuery"] = query

        _uiState.value = _uiState.value.copy(
            speciesQuery = query,
            errorMessage = null
        )
    }

    fun searchSpecies() {
        val query = _uiState.value.speciesQuery.trim()

        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Enter a species name."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearchingSpecies = true,
                errorMessage = null,
                speciesResults = emptyList()
            )

            speciesRepository.searchSpecies(query).fold(
                onSuccess = { results ->
                    _uiState.value = _uiState.value.copy(
                        isSearchingSpecies = false,
                        speciesResults = results,
                        errorMessage = if (results.isEmpty()) {
                            "No species found. Try another name."
                        } else {
                            null
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearchingSpecies = false,
                        errorMessage = error.message
                            ?: "Unable to search species."
                    )
                }
            )
        }
    }

    fun onSpeciesSelected(species: Species) {
        savedStateHandle["selectedSpeciesId"] = species.id
        savedStateHandle["selectedSpeciesScientificName"] =
            species.scientificName
        savedStateHandle["selectedSpeciesCommonName"] =
            species.commonName
        savedStateHandle["selectedSpeciesPhotoUrl"] =
            species.photoUrl

        _uiState.value = _uiState.value.copy(
            species = species,
            errorMessage = null
        )
    }

    fun onLatitudeChanged(latitude: String) {
        savedStateHandle["latitude"] = latitude

        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            errorMessage = null
        )
    }

    fun onLongitudeChanged(longitude: String) {
        savedStateHandle["longitude"] = longitude

        _uiState.value = _uiState.value.copy(
            longitude = longitude,
            errorMessage = null
        )
    }

    fun onNotesChanged(notes: String) {
        savedStateHandle["notes"] = notes

        _uiState.value = _uiState.value.copy(
            notes = notes
        )
    }

    fun onPhotoSelected(photoUri: String?) {
        savedStateHandle["photoUri"] = photoUri

        _uiState.value = _uiState.value.copy(
            photoUri = photoUri
        )
    }

    fun onGalleryPhotoPicked(source: Uri?) {
        if (source == null) return

        viewModelScope.launch {
            val storedUri = photoStorage.copyFromGallery(source)
            if (storedUri != null) {
                onPhotoSelected(storedUri)
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Couldn't load that photo. Please try another one."
                )
            }
        }
    }

    fun prepareCameraPhoto(): Uri? =
        try {
            val target = photoStorage.createCameraTarget()
            savedStateHandle["pendingCameraPath"] = target.path
            target.contentUri
        } catch (_: IOException) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Couldn't prepare the camera. Check device storage."
            )
            null
        }

    fun onCameraResult(success: Boolean) {
        val path = savedStateHandle.get<String>("pendingCameraPath") ?: return
        savedStateHandle.remove<String>("pendingCameraPath")

        val storedUri = photoStorage.finishCameraCapture(path, success)
        if (storedUri != null) onPhotoSelected(storedUri)
    }

    fun onCameraUnavailable() {
        onCameraResult(false)
        _uiState.value = _uiState.value.copy(
            errorMessage = "No camera app is available on this device."
        )
    }

    fun onTimestampChanged(timestamp: Long) {
        savedStateHandle["timestamp"] = timestamp

        _uiState.value = _uiState.value.copy(
            timestamp = timestamp
        )
    }

    fun loadForEditing(sightingId: Long) {
        editingSightingId = sightingId

        viewModelScope.launch {
            try {
                val sighting =
                    sightingRepository.getSightingById(sightingId)

                if (sighting == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Sighting not found."
                    )
                    return@launch
                }

                val species = speciesRepository
                    .getSavedSpeciesById(sighting.speciesId)

                _uiState.value = _uiState.value.copy(
                    species = species,
                    latitude = sighting.latitude.toString(),
                    longitude = sighting.longitude.toString(),
                    notes = sighting.notes.orEmpty(),
                    photoUri = sighting.photoUri,
                    timestamp = sighting.timestamp,
                    errorMessage = null
                )

                savedStateHandle["latitude"] =
                    sighting.latitude.toString()
                savedStateHandle["longitude"] =
                    sighting.longitude.toString()
                savedStateHandle["notes"] =
                    sighting.notes.orEmpty()
                savedStateHandle["photoUri"] =
                    sighting.photoUri
                savedStateHandle["timestamp"] =
                    sighting.timestamp

                if (species != null) {
                    onSpeciesSelected(species)
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message
                        ?: "Unable to load sighting."
                )
            }
        }
    }

    fun save() {
        val currentState = _uiState.value

        val species = currentState.species
        val latitude = CoordinateParser.parse(currentState.latitude, "NS")
        val longitude = CoordinateParser.parse(currentState.longitude, "EW")

        when {
            species == null -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Please select a species."
                )
                return
            }

            latitude == null || latitude !in -90.0..90.0 -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Enter a valid latitude."
                )
                return
            }

            longitude == null || longitude !in -180.0..180.0 -> {
                _uiState.value = currentState.copy(
                    errorMessage = "Enter a valid longitude."
                )
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSaving = true,
                errorMessage = null,
                savedSightingId = null
            )

            try {
                speciesRepository.saveSpecies(species)

                if (editingSightingId == null) {
                    val sighting = SightingEntity(
                        speciesId = species.id,
                        latitude = latitude!!,
                        longitude = longitude!!,
                        timestamp = currentState.timestamp,
                        photoUri = currentState.photoUri,
                        notes = currentState.notes.ifBlank { null }
                    )

                    val id =
                        sightingRepository.insertSighting(sighting)

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedSightingId = id
                    )
                } else {
                    val existing = sightingRepository
                        .getSightingById(editingSightingId!!)

                    if (existing == null) {
                        _uiState.value = _uiState.value.copy(
                            isSaving = false,
                            errorMessage = "Sighting not found."
                        )
                        return@launch
                    }

                    val updated = existing.copy(
                        speciesId = species.id,
                        latitude = latitude!!,
                        longitude = longitude!!,
                        timestamp = currentState.timestamp,
                        photoUri = currentState.photoUri,
                        notes = currentState.notes.ifBlank { null }
                    )

                    sightingRepository.updateSighting(updated)

                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        savedSightingId = updated.id
                    )
                }
            } catch (error: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = error.message
                        ?: "Unable to save sighting."
                )
            }
        }
    }
}

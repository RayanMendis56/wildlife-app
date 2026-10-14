
package com.example.wildlife_app.ui.sightinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.data.repository.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

sealed interface SightingListUiState {
    data object Loading : SightingListUiState
    data object Empty : SightingListUiState

    data class Success(
        val sightings: List<SightingWithSpecies>
    ) : SightingListUiState

    data class Error(
        val message: String
    ) : SightingListUiState
}

class SightingListViewModel(
    private val repository: SightingRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SightingListUiState>(
            SightingListUiState.Loading
        )

    val uiState: StateFlow<SightingListUiState> =
        _uiState.asStateFlow()

    private val _preferCommonName = MutableStateFlow(true)

    val preferCommonName: StateFlow<Boolean> =
        _preferCommonName.asStateFlow()

    init {
        observeSightings()
        observeNamePreference()
    }

    private fun observeSightings() {
        viewModelScope.launch {
            repository.observeAllSightings()
                .onStart {
                    _uiState.value = SightingListUiState.Loading
                }
                .catch { error ->
                    _uiState.value = SightingListUiState.Error(
                        error.message ?: "Unable to load sightings."
                    )
                }
                .collect { sightings ->
                    _uiState.value =
                        if (sightings.isEmpty()) {
                            SightingListUiState.Empty
                        } else {
                            SightingListUiState.Success(sightings)
                        }
                }
        }
    }

    private fun observeNamePreference() {
        viewModelScope.launch {
            preferencesRepository.preferCommonName
                .catch {
                    _preferCommonName.value = true
                }
                .collect { preferCommon ->
                    _preferCommonName.value = preferCommon
                }
        }
    }

    fun deleteSighting(sighting: SightingWithSpecies) {
        viewModelScope.launch {
            try {
                repository.deleteSighting(sighting.sighting)
            } catch (error: Exception) {
                _uiState.value = SightingListUiState.Error(
                    error.message ?: "Unable to delete sighting."
                )
            }
        }
    }
}

package com.example.wildlife_app.ui.speciessearch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.repository.SpeciesRepository
import com.example.wildlife_app.domain.model.Species
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SpeciesSearchUiState {

    data object Idle : SpeciesSearchUiState

    data object Loading : SpeciesSearchUiState

    data class Success(
        val species: List<Species>
    ) : SpeciesSearchUiState

    data object Empty : SpeciesSearchUiState

    data class Error(
        val message: String
    ) : SpeciesSearchUiState
}

class SpeciesSearchViewModel(
    private val repository: SpeciesRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")

    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState =
        MutableStateFlow<SpeciesSearchUiState>(
            SpeciesSearchUiState.Idle
        )

    val uiState: StateFlow<SpeciesSearchUiState> =
        _uiState.asStateFlow()

    fun onQueryChanged(query: String) {
        _query.value = query
    }

    fun searchSpecies() {
        val searchQuery = _query.value.trim()

        if (searchQuery.isBlank()) {
            _uiState.value = SpeciesSearchUiState.Empty
            return
        }

        viewModelScope.launch {
            _uiState.value = SpeciesSearchUiState.Loading

            try {
                val result = repository.searchSpecies(searchQuery)

                result.fold(
                    onSuccess = { species ->
                        _uiState.value =
                            if (species.isEmpty()) {
                                SpeciesSearchUiState.Empty
                            } else {
                                SpeciesSearchUiState.Success(species)
                            }
                    },
                    onFailure = { error ->
                        _uiState.value =
                            SpeciesSearchUiState.Error(
                                error.message
                                    ?: "Unable to search for species."
                            )
                    }
                )
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _uiState.value =
                    SpeciesSearchUiState.Error(
                        error.message
                            ?: "Unable to search for species."
                    )
            }
        }
    }

    fun retrySearch() {
        searchSpecies()
    }
}
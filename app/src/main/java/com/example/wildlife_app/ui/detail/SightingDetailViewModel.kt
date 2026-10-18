
package com.example.wildlife_app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.data.repository.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed interface SightingDetailUiState {
    data object Loading : SightingDetailUiState

    data class Success(
        val sighting: SightingWithSpecies
    ) : SightingDetailUiState

    data object NotFound : SightingDetailUiState

    data class Error(
        val message: String
    ) : SightingDetailUiState
}

class SightingDetailViewModel(
    private val repository: SightingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SightingDetailUiState>(
            SightingDetailUiState.Loading
        )

    val uiState: StateFlow<SightingDetailUiState> =
        _uiState.asStateFlow()

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteResult = MutableStateFlow<String?>(null)
    val deleteResult: StateFlow<String?> = _deleteResult.asStateFlow()

    fun loadSighting(sightingId: Long) {
        viewModelScope.launch {
            repository.observeSightingById(sightingId)
                .catch { error ->
                    _uiState.value = SightingDetailUiState.Error(
                        error.message ?: "Unable to load sighting."
                    )
                }
                .collect { sighting ->
                    _uiState.value = if (sighting == null) {
                        SightingDetailUiState.NotFound
                    } else {
                        SightingDetailUiState.Success(sighting)
                    }
                }
        }
    }

    fun deleteSighting(sighting: SightingWithSpecies) {
        if (_isDeleting.value) return

        viewModelScope.launch {
            _isDeleting.value = true
            _deleteResult.value = null

            try {
                repository.deleteSighting(sighting.sighting)
                _deleteResult.value = "SUCCESS"
            } catch (error: Exception) {
                _deleteResult.value =
                    error.message ?: "Unable to delete sighting."
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}

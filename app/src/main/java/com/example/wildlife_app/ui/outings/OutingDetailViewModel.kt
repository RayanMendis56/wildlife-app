
package com.example.wildlife_app.ui.outings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.local.entity.OutingEntity
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.data.repository.OutingRepository
import com.example.wildlife_app.data.repository.SightingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

sealed interface OutingDetailUiState {
    data object Loading : OutingDetailUiState

    data class Success(
        val outing: OutingEntity,
        val sightings: List<SightingWithSpecies>
    ) : OutingDetailUiState

    data class Error(val message: String) : OutingDetailUiState
}

class OutingDetailViewModel(
    private val outingId: Long,
    private val outingRepository: OutingRepository,
    private val sightingRepository: SightingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<OutingDetailUiState>(OutingDetailUiState.Loading)

    val uiState: StateFlow<OutingDetailUiState> = _uiState.asStateFlow()

    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()

    init {
        observeOutingDetails()
    }

    private fun observeOutingDetails() {
        viewModelScope.launch {
            outingRepository.observeAllOutings()
                .combine(
                    sightingRepository.observeSightingsByOuting(outingId)
                ) { outings, sightings ->
                    val outing = outings.firstOrNull { it.id == outingId }

                    if (outing == null) {
                        OutingDetailUiState.Error(
                            "This outing could not be found."
                        )
                    } else {
                        OutingDetailUiState.Success(outing, sightings)
                    }
                }
                .catch { exception ->
                    _uiState.value = OutingDetailUiState.Error(
                        exception.message
                            ?: "Unable to load outing details."
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun updateOuting(
        name: String,
        date: Long,
        onSaved: () -> Unit
    ) {
        val currentState = _uiState.value

        if (currentState !is OutingDetailUiState.Success) {
            _operationMessage.value =
                "Outing details are not ready yet."
            return
        }

        val trimmedName = name.trim()

        if (trimmedName.isBlank()) {
            _operationMessage.value = "Please enter an outing name."
            return
        }

        viewModelScope.launch {
            try {
                outingRepository.saveOuting(
                    currentState.outing.copy(
                        name = trimmedName,
                        date = date
                    )
                )

                _operationMessage.value = null
                onSaved()
            } catch (exception: Exception) {
                _operationMessage.value =
                    exception.message ?: "Unable to update outing."
            }
        }
    }

    fun deleteOuting(onDeleted: () -> Unit) {
        val currentState = _uiState.value

        if (currentState !is OutingDetailUiState.Success) {
            _operationMessage.value =
                "Outing details are not ready yet."
            return
        }

        viewModelScope.launch {
            try {
                outingRepository.deleteOuting(currentState.outing)

                _operationMessage.value = null
                onDeleted()
            } catch (exception: Exception) {
                _operationMessage.value =
                    exception.message ?: "Unable to delete outing."
            }
        }
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }
}

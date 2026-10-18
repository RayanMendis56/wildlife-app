
package com.example.wildlife_app.ui.outings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.local.entity.OutingEntity
import com.example.wildlife_app.data.repository.OutingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OutingsUiState {
    data object Loading : OutingsUiState

    data class Success(
        val outings: List<OutingEntity>
    ) : OutingsUiState

    data class Error(
        val message: String
    ) : OutingsUiState
}

class OutingsViewModel(
    private val repository: OutingRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<OutingsUiState>(OutingsUiState.Loading)

    val uiState: StateFlow<OutingsUiState> =
        _uiState.asStateFlow()

    init {
        observeOutings()
    }

    private fun observeOutings() {
        viewModelScope.launch {
            try {
                repository.observeAllOutings().collect { outings ->
                    _uiState.value = OutingsUiState.Success(outings)
                }
            } catch (exception: Exception) {
                _uiState.value = OutingsUiState.Error(
                    exception.message ?: "Unable to load outings."
                )
            }
        }
    }

    fun addOuting(
        name: String,
        date: Long,
        onSaved: () -> Unit,
        onError: (String) -> Unit
    ) {
        val outingName = name.trim()

        if (outingName.isBlank()) {
            onError("Please enter an outing name.")
            return
        }

        viewModelScope.launch {
            try {
                repository.saveOuting(
                    OutingEntity(
                        name = outingName,
                        date = date
                    )
                )
                onSaved()
            } catch (exception: Exception) {
                onError(
                    exception.message ?: "Unable to save the outing."
                )
            }
        }
    }
}

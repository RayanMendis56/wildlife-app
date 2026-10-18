
package com.example.wildlife_app.ui.sightinglist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.data.repository.SightingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

enum class SightingSortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

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
        MutableStateFlow<SightingListUiState>(SightingListUiState.Loading)

    val uiState: StateFlow<SightingListUiState> =
        _uiState.asStateFlow()

    private val _preferCommonName = MutableStateFlow(true)

    val preferCommonName: StateFlow<Boolean> =
        _preferCommonName.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    private val _sortOrder =
        MutableStateFlow(SightingSortOrder.NEWEST_FIRST)

    val sortOrder: StateFlow<SightingSortOrder> =
        _sortOrder.asStateFlow()

    private val _deletedSighting =
        MutableStateFlow<SightingWithSpecies?>(null)

    val deletedSighting: StateFlow<SightingWithSpecies?> =
        _deletedSighting.asStateFlow()

    private var allSightings: List<SightingWithSpecies> = emptyList()
    private var deletionJob: Job? = null
    private var deletionOperationJob: Job? = null
    private var loadError: String? = null

    init {
        observeSightings()
        observeNamePreference()
        observeSortPreference()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        updateVisibleSightings()
    }

    fun onSortOrderChanged(order: SightingSortOrder) {
        _sortOrder.value = order
        updateVisibleSightings()

        viewModelScope.launch {
            try {
                preferencesRepository.setSightingSortOrder(order.name)
            } catch (error: Exception) {
                _uiState.value = SightingListUiState.Error(
                    error.message ?: "Unable to save sort preference."
                )
            }
        }
    }

    private fun observeSightings() {
        viewModelScope.launch {
            repository.observeAllSightings()
                .onStart {
                    _uiState.value = SightingListUiState.Loading
                }
                .catch { error ->
                    loadError =
                        error.message ?: "Unable to load sightings."

                    _uiState.value =
                        SightingListUiState.Error(loadError!!)
                }
                .collect { sightings ->
                    loadError = null
                    allSightings = sightings
                    updateVisibleSightings()
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

    private fun observeSortPreference() {
        viewModelScope.launch {
            preferencesRepository.sightingSortOrder
                .catch {
                    _sortOrder.value = SightingSortOrder.NEWEST_FIRST
                    updateVisibleSightings()
                }
                .collect { savedOrder ->
                    _sortOrder.value =
                        SightingSortOrder.entries.firstOrNull {
                            it.name == savedOrder
                        } ?: SightingSortOrder.NEWEST_FIRST

                    updateVisibleSightings()
                }
        }
    }

    private fun updateVisibleSightings() {
        if (loadError != null) return

        val query = _searchQuery.value.trim()

        val filtered = allSightings.filter { item ->
            query.isBlank() ||
                    item.species.commonName.orEmpty()
                        .contains(query, ignoreCase = true) ||
                    item.species.scientificName
                        .contains(query, ignoreCase = true) ||
                    item.sighting.notes.orEmpty()
                        .contains(query, ignoreCase = true) ||
                    item.sighting.latitude.toString()
                        .contains(query, ignoreCase = true) ||
                    item.sighting.longitude.toString()
                        .contains(query, ignoreCase = true)
        }

        val sorted = when (_sortOrder.value) {
            SightingSortOrder.NEWEST_FIRST ->
                filtered.sortedByDescending {
                    it.sighting.timestamp
                }

            SightingSortOrder.OLDEST_FIRST ->
                filtered.sortedBy {
                    it.sighting.timestamp
                }
        }

        _uiState.value = if (sorted.isEmpty()) {
            SightingListUiState.Empty
        } else {
            SightingListUiState.Success(sorted)
        }
    }

    fun deleteSighting(sighting: SightingWithSpecies) {
        deletionOperationJob?.cancel()

        deletionOperationJob = viewModelScope.launch {
            try {
                repository.deleteSighting(sighting.sighting)

                deletionJob?.cancel()
                _deletedSighting.value = sighting

                deletionJob = launch {
                    delay(5_000)

                    if (_deletedSighting.value?.sighting?.id ==
                        sighting.sighting.id
                    ) {
                        _deletedSighting.value = null
                    }
                }
            } catch (error: Exception) {
                _uiState.value = SightingListUiState.Error(
                    error.message ?: "Unable to delete sighting."
                )
            }
        }
    }

    fun undoDelete() {
        val deleted = _deletedSighting.value ?: return

        deletionJob?.cancel()

        deletionOperationJob?.cancel()
        deletionOperationJob = viewModelScope.launch {
            try {
                repository.restoreSighting(deleted.sighting)
                _deletedSighting.value = null
            } catch (error: Exception) {
                _uiState.value = SightingListUiState.Error(
                    error.message ?: "Unable to restore sighting."
                )
            }
        }
    }

    override fun onCleared() {
        deletionJob?.cancel()
        deletionOperationJob?.cancel()
        super.onCleared()
    }
}

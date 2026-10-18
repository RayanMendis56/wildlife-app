package com.example.wildlife_app.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.data.local.AppDatabase
import com.example.wildlife_app.data.local.relation.SightingWithSpecies
import com.example.wildlife_app.data.repository.SightingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

/** What the dashboard shows for one recent sighting. Naming rules are already applied. */
data class RecentSightingUi(
    val id: Long,
    val title: String,
    val subtitle: String?,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String?
)

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(
        val totalSightings: Int,
        val speciesCount: Int,
        val thisMonthCount: Int,
        val recent: List<RecentSightingUi>
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: SightingRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Bumped by retry() to restart the flow below.
    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = retryTrigger
        .flatMapLatest {
            combine(
                repository.observeAllSightings(),
                preferencesRepository.preferCommonName
            ) { sightings, preferCommon ->
                buildState(sightings, preferCommon)
            }.catch { error ->
                emit(HomeUiState.Error(error.message ?: "Unable to load your sightings."))
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading
        )

    fun retry() {
        retryTrigger.value += 1
    }

    // All dashboard rules live here: counting, "this month", and which name to show.
    private fun buildState(
        sightings: List<SightingWithSpecies>,
        preferCommon: Boolean
    ): HomeUiState {
        if (sightings.isEmpty()) return HomeUiState.Empty

        val monthStart = startOfCurrentMonth()

        return HomeUiState.Success(
            totalSightings = sightings.size,
            speciesCount = sightings.map { it.species.id }.distinct().size,
            thisMonthCount = sightings.count { it.sighting.timestamp >= monthStart },
            recent = sightings.take(RECENT_COUNT).map { item ->
                val scientific = item.species.scientificName
                val common = item.species.commonName?.takeIf { it.isNotBlank() }
                val title = if (preferCommon) common ?: scientific else scientific
                RecentSightingUi(
                    id = item.sighting.id,
                    title = title,
                    subtitle = if (title != scientific) scientific else null,
                    timestamp = item.sighting.timestamp,
                    latitude = item.sighting.latitude,
                    longitude = item.sighting.longitude,
                    photoUri = item.sighting.photoUri
                )
            }
        )
    }

    private fun startOfCurrentMonth(): Long =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private companion object {
        const val RECENT_COUNT = 5
    }
}

class HomeViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val dao = AppDatabase.getInstance(appContext).sightingDao()
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                SightingRepository(dao),
                UserPreferencesRepository(appContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
package com.example.wildlife_app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferCommonName: StateFlow<Boolean> =
        preferencesRepository.preferCommonName.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true
        )

    fun setPreferCommonName(preferCommonName: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPreferCommonName(
                preferCommonName
            )
        }
    }
}
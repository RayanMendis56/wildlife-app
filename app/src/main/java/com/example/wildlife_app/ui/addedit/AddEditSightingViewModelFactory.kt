
package com.example.wildlife_app.ui.addedit

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wildlife_app.data.local.AppDatabase
import com.example.wildlife_app.data.local.PhotoStorage
import com.example.wildlife_app.data.remote.NetworkModule
import com.example.wildlife_app.data.repository.SightingRepository
import com.example.wildlife_app.data.repository.SpeciesRepository

class AddEditSightingViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(AddEditSightingViewModel::class.java)) {
            val database = AppDatabase.getInstance(appContext)

            val sightingRepository = SightingRepository(
                database.sightingDao()
            )

            val speciesRepository = SpeciesRepository(
                NetworkModule.api,
                database.speciesDao()
            )

            val viewModel = AddEditSightingViewModel(
                SavedStateHandle(),
                sightingRepository,
                speciesRepository,
                PhotoStorage(appContext)
            )

            @Suppress("UNCHECKED_CAST")
            return viewModel as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

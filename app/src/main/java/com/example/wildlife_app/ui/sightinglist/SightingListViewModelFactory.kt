
package com.example.wildlife_app.ui.sightinglist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.data.local.AppDatabase
import com.example.wildlife_app.data.repository.SightingRepository

class SightingListViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SightingListViewModel::class.java)) {
            val database = AppDatabase.getInstance(appContext)
            val sightingRepository =
                SightingRepository(database.sightingDao())
            val preferencesRepository =
                UserPreferencesRepository(appContext)

            @Suppress("UNCHECKED_CAST")
            return SightingListViewModel(
                sightingRepository,
                preferencesRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

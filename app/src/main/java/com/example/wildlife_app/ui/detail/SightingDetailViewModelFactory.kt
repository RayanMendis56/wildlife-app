
package com.example.wildlife_app.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wildlife_app.data.local.AppDatabase
import com.example.wildlife_app.data.repository.SightingRepository

class SightingDetailViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(SightingDetailViewModel::class.java)) {
            val database = AppDatabase.getInstance(appContext)
            val repository = SightingRepository(database.sightingDao())

            @Suppress("UNCHECKED_CAST")
            return SightingDetailViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}

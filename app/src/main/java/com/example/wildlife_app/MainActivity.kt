package com.example.wildlife_app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.wildlife_app.data.local.AppDatabase
import com.example.wildlife_app.data.remote.NetworkModule
import com.example.wildlife_app.data.repository.SpeciesRepository
import com.example.wildlife_app.ui.theme.WildlifeappTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // TEMPORARY: API test. Remove before committing.
        lifecycleScope.launch {
            val repo = SpeciesRepository(
                NetworkModule.api,
                AppDatabase.getInstance(this@MainActivity).speciesDao()
            )
            Log.d("API_TEST", repo.searchSpecies("robin").toString())
        }

        setContent {
            WildlifeappTheme {
                // App content will go here
            }
        }
    }
}
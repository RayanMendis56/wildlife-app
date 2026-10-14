
package com.example.wildlife_app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.ui.addedit.AddEditSightingScreen
import com.example.wildlife_app.ui.addedit.AddEditSightingViewModel
import com.example.wildlife_app.ui.addedit.AddEditSightingViewModelFactory
import com.example.wildlife_app.ui.detail.SightingDetailScreen
import com.example.wildlife_app.ui.detail.SightingDetailViewModel
import com.example.wildlife_app.ui.detail.SightingDetailViewModelFactory
import com.example.wildlife_app.ui.settings.SettingsScreen
import com.example.wildlife_app.ui.settings.SettingsViewModel
import com.example.wildlife_app.ui.sightinglist.SightingListScreen
import com.example.wildlife_app.ui.sightinglist.SightingListViewModel
import com.example.wildlife_app.ui.sightinglist.SightingListViewModelFactory

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val sightingListViewModel: SightingListViewModel = viewModel(
        factory = SightingListViewModelFactory(context)
    )

    NavHost(
        navController = navController,
        startDestination = Destinations.SIGHTING_LIST
    ) {
        composable(Destinations.SIGHTING_LIST) {

            SightingListScreen(
                viewModel = sightingListViewModel,
                onAddSighting = {
                    navController.navigate(Destinations.ADD_SIGHTING)
                },
                onSightingClick = { sightingId ->
                    navController.navigate("sighting_detail/$sightingId")
                },
                onSettingsClick = {
                    navController.navigate(Destinations.SETTINGS)
                }
            )

        }

        composable(Destinations.ADD_SIGHTING) {
            val addEditViewModel: AddEditSightingViewModel = viewModel(
                factory = AddEditSightingViewModelFactory(context)
            )

            AddEditSightingScreen(
                viewModel = addEditViewModel,
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Destinations.SIGHTING_DETAIL,
            arguments = listOf(
                navArgument("sightingId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val sightingId = backStackEntry.arguments
                ?.getLong("sightingId") ?: return@composable

            val detailViewModel: SightingDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = SightingDetailViewModelFactory(context)
            )

            SightingDetailScreen(
                sightingId = sightingId,
                viewModel = detailViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.SETTINGS) {
            val preferencesRepository = UserPreferencesRepository(context)

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(
                        modelClass: Class<T>
                    ): T {
                        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return SettingsViewModel(preferencesRepository) as T
                        }
                        throw IllegalArgumentException(
                            "Unknown ViewModel class: ${modelClass.name}"
                        )
                    }
                }
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

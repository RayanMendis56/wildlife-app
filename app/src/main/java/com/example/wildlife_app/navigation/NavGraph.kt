package com.example.wildlife_app.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wildlife_app.data.datastore.UserPreferencesRepository
import com.example.wildlife_app.ui.addedit.AddEditSightingScreen
import com.example.wildlife_app.ui.addedit.AddEditSightingViewModel
import com.example.wildlife_app.ui.addedit.AddEditSightingViewModelFactory
import com.example.wildlife_app.ui.detail.SightingDetailScreen
import com.example.wildlife_app.ui.detail.SightingDetailViewModel
import com.example.wildlife_app.ui.detail.SightingDetailViewModelFactory
import com.example.wildlife_app.ui.home.HomeScreen
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

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The bar or rail appears only on the top-level screens, not on forms or detail.
    val showNavigation = topLevelDestinations.any {
        currentDestination.isInHierarchyOf(it.route)
    }

    // Wide windows (phone landscape, tablets) get a side rail; portrait phones get a bottom bar.
    val useRail = LocalConfiguration.current.screenWidthDp >= 600

    fun navigateToTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Row(modifier = Modifier.fillMaxSize()) {
        if (showNavigation && useRail) {
            AppNavigationRail(
                currentDestination = currentDestination,
                onNavigate = ::navigateToTopLevel
            )
        }

        Scaffold(
            modifier = Modifier.weight(1f),
            bottomBar = {
                if (showNavigation && !useRail) {
                    AppNavigationBar(
                        currentDestination = currentDestination,
                        onNavigate = ::navigateToTopLevel
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Destinations.HOME,
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()   // keeps form fields above the keyboard
            ) {
                composable(Destinations.HOME) {
                    HomeScreen(
                        onAddSighting = { navController.navigate(Destinations.ADD_SIGHTING) },
                        onViewSightings = { navigateToTopLevel(Destinations.SIGHTING_LIST) }
                    )
                }

                composable(Destinations.SIGHTING_LIST) {
                    SightingListScreen(
                        viewModel = sightingListViewModel,
                        onAddSighting = { navController.navigate(Destinations.ADD_SIGHTING) },
                        onSightingClick = { id ->
                            navController.navigate(Destinations.sightingDetail(id))
                        },
                        onSettingsClick = { navigateToTopLevel(Destinations.SETTINGS) }
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

                // NEW: editing an existing sighting (R3 "update data")
                composable(
                    route = Destinations.EDIT_SIGHTING,
                    arguments = listOf(
                        navArgument("sightingId") { type = NavType.LongType }
                    )
                ) { entry ->
                    val sightingId = entry.arguments?.getLong("sightingId")
                        ?: return@composable

                    val editViewModel: AddEditSightingViewModel = viewModel(
                        factory = AddEditSightingViewModelFactory(context)
                    )

                    LaunchedEffect(sightingId) {
                        editViewModel.loadForEditing(sightingId)
                    }

                    AddEditSightingScreen(
                        viewModel = editViewModel,
                        onSaved = { navController.popBackStack() },
                        onCancel = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Destinations.SIGHTING_DETAIL,
                    arguments = listOf(
                        navArgument("sightingId") { type = NavType.LongType }
                    )
                ) { entry ->
                    val sightingId = entry.arguments?.getLong("sightingId")
                        ?: return@composable

                    val detailViewModel: SightingDetailViewModel = viewModel(
                        viewModelStoreOwner = entry,
                        factory = SightingDetailViewModelFactory(context)
                    )

                    SightingDetailScreen(
                        sightingId = sightingId,
                        viewModel = detailViewModel,
                        onBack = { navController.popBackStack() },
                        onEdit = {
                            navController.navigate(Destinations.editSighting(sightingId))
                        }
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
    }
}
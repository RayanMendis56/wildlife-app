
package com.example.wildlife_app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy

data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val topLevelDestinations = listOf(
    TopLevelDestination(
        Destinations.HOME,
        "Home",
        Icons.Outlined.Home
    ),
    TopLevelDestination(
        Destinations.SIGHTING_LIST,
        "Sightings",
        Icons.Outlined.Pets
    ),
    TopLevelDestination(
        Destinations.OUTINGS,
        "Outings",
        Icons.Outlined.Explore
    ),
    TopLevelDestination(
        Destinations.SETTINGS,
        "Settings",
        Icons.Outlined.Settings
    )
)

fun NavDestination?.isInHierarchyOf(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true

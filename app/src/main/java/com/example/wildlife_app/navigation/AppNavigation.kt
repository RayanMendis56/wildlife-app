package com.example.wildlife_app.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination

@Composable
fun AppNavigationBar(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination.isInHierarchyOf(destination.route),
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) }   // the label names the control for screen readers
            )
        }
    }
}

@Composable
fun AppNavigationRail(
    currentDestination: NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationRail {
        topLevelDestinations.forEach { destination ->
            NavigationRailItem(
                selected = currentDestination.isInHierarchyOf(destination.route),
                onClick = { onNavigate(destination.route) },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(destination.label) }
            )
        }
    }
}
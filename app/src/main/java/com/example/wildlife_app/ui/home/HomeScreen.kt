package com.example.wildlife_app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onAddSighting: () -> Unit,
    onViewSightings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Wildlife Log",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Record what you see on your walks and hikes, with a photo and a place.",
            style = MaterialTheme.typography.bodyLarge
        )
        Button(onClick = onAddSighting, modifier = Modifier.fillMaxWidth()) {
            Text("Add sighting")
        }
        OutlinedButton(onClick = onViewSightings, modifier = Modifier.fillMaxWidth()) {
            Text("View all sightings")
        }
    }
}
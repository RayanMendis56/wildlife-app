package com.example.wildlife_app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.wildlife_app.ui.components.EmptyState
import com.example.wildlife_app.ui.components.ErrorState
import com.example.wildlife_app.ui.components.LoadingState
import com.example.wildlife_app.ui.components.SectionHeader
import com.example.wildlife_app.ui.components.SightingListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddSighting: () -> Unit,
    onViewSightings: () -> Unit,
    onSightingClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        HomeUiState.Loading -> LoadingState(message = "Loading your dashboard…")

        is HomeUiState.Error -> ErrorState(
            message = state.message,
            onRetry = viewModel::retry
        )

        HomeUiState.Empty -> EmptyState(
            title = "No sightings yet",
            message = "Log your first wildlife sighting with a photo and a place.",
            actionLabel = "Add sighting",
            onAction = onAddSighting
        )

        is HomeUiState.Success -> HomeContent(
            state = state,
            onAddSighting = onAddSighting,
            onViewSightings = onViewSightings,
            onSightingClick = onSightingClick
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onAddSighting: () -> Unit,
    onViewSightings: () -> Unit,
    onSightingClick: (Long) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("d MMM yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Wildlife Log",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Your recent encounters at a glance.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Sightings", state.totalSightings, Modifier.weight(1f))
                StatCard("Species", state.speciesCount, Modifier.weight(1f))
                StatCard("This month", state.thisMonthCount, Modifier.weight(1f))
            }
        }

        item {
            Button(
                onClick = onAddSighting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Text("Add sighting", modifier = Modifier.padding(start = 8.dp))
            }
        }

        item { SectionHeader("Recent sightings") }

        items(items = state.recent, key = { it.id }) { recent ->
            SightingListItem(
                title = recent.title,
                subtitle = recent.subtitle,
                supportingText = "${dateFormat.format(Date(recent.timestamp))} · " +
                        "${"%.4f".format(recent.latitude)}, ${"%.4f".format(recent.longitude)}",
                photoUri = recent.photoUri,
                onClick = { onSightingClick(recent.id) }
            )
        }

        item {
            TextButton(
                onClick = onViewSightings,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View all sightings")
            }
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics(mergeDescendants = true) {},   // read as "12 Sightings"
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
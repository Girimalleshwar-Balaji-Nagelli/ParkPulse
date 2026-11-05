package com.parkpulse.app.presentation.parks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.presentation.parks.components.ParkCard
import com.parkpulse.app.presentation.parks.components.ParkSearchBar
import com.parkpulse.app.presentation.parks.components.ParkStatusFilter
import com.parkpulse.app.presentation.theme.ParksColors

@Composable
fun ParksScreen(
    modifier: Modifier = Modifier,
    viewModel: ParksViewModel = hiltViewModel(),
    onProfileClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ParksContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onRetry = viewModel::retry,
        onProfileClick = onProfileClick,
        modifier = modifier
    )
}

@Composable
private fun ParksContent(
    state: ParksUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (StatusFilter) -> Unit,
    onRetry: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(modifier = modifier, containerColor = ParksColors.BackgroundMint) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Parks", style = MaterialTheme.typography.headlineMedium, color = ParksColors.DarkGreen)
                    Text(
                        "Explore and manage city parks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ParksColors.SecondaryGreen
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(Icons.Default.Person, contentDescription = "Profile", tint = ParksColors.DarkGreen)
                }
            }

            Spacer(Modifier.height(16.dp))
            ParkSearchBar(query = state.query, onQueryChange = onQueryChange)
            Spacer(Modifier.height(12.dp))
            ParkStatusFilter(selected = state.filter, onSelected = onFilterChange)
            Spacer(Modifier.height(12.dp))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    state.isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = ParksColors.DarkGreen
                    )
                    state.error != null -> MessageState(
                        title = "Couldn't load parks",
                        message = state.error,
                        actionLabel = "Retry",
                        onAction = onRetry
                    )
                    state.isEmpty -> MessageState(
                        title = "No parks yet",
                        message = "Parks will appear here once they are added."
                    )
                    state.noResults -> MessageState(
                        title = "No matching parks",
                        message = "Try a different name or status filter."
                    )
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(state.parks, key = { it.id }) { park -> ParkCard(park) }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.MessageState(
    title: String,
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = Modifier.align(Alignment.Center).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = ParksColors.DarkGreen)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = ParksColors.SecondaryGreen,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParksColors.DarkGreen,
                    contentColor = ParksColors.White
                )
            ) { Text(actionLabel) }
        }
    }
}
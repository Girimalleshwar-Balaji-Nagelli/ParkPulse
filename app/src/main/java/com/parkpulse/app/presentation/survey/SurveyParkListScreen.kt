package com.parkpulse.app.presentation.survey

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.presentation.theme.ParksColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyParkListScreen(
    onBack: () -> Unit,
    onParkSelected: (String) -> Unit,
    viewModel: SurveyParkListViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ParksColors.BackgroundMint,
        topBar = {
            TopAppBar(
                title = { Text("Select a park to survey") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ParksColors.White,
                    titleContentColor = ParksColors.DarkGreen,
                    navigationIconContentColor = ParksColors.DarkGreen,
                ),
            )
        },
    ) { padding ->
        val list = items
        when {
            list == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ParksColors.PrimaryGreen)
            }
            list.isEmpty() -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No parks yet. Create a park first.", color = ParksColors.DarkGreen)
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(list, key = { it.id }) { item ->
                    ParkRow(item, onClick = { onParkSelected(item.id) })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParkRow(item: SurveyParkItem, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        ParkStatus.OPEN -> ParksColors.StatusOpen
        ParkStatus.NEEDS_REPAIR -> ParksColors.StatusRepair
        ParkStatus.CLOSED -> ParksColors.StatusClosed
    }
    Card(
        onClick = onClick,
        enabled = item.canSurvey,
        modifier = Modifier.fillMaxWidth().alpha(if (item.canSurvey) 1f else 0.55f),
        colors = CardDefaults.cardColors(
            containerColor = ParksColors.White,
            disabledContainerColor = ParksColors.White,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(statusColor))
            Column(Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ParksColors.DarkGreen,
                )
                Text(
                    "${item.typeLabel} · ${item.status.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ParksColors.SecondaryGreen,
                )
                if (!item.canSurvey) {
                    Text(
                        "No verified boundary, so check-in is not possible",
                        style = MaterialTheme.typography.bodySmall,
                        color = ParksColors.ErrorRed,
                    )
                }
            }
        }
    }
}

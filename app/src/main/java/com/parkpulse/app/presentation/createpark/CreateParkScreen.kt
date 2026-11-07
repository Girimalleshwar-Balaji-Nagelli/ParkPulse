package com.parkpulse.app.presentation.createpark

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.presentation.createpark.components.BoundaryEditorMap
import com.parkpulse.app.presentation.theme.ParksColors
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateParkScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CreateParkViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreateParkEvent.Saved -> onSaved()
                is CreateParkEvent.Failed -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = ParksColors.BackgroundMint,
        topBar = {
            TopAppBar(
                title = { Text("Create park") },
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
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().imePadding()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                BoundaryEditorMap(
                    points = state.points,
                    isValid = state.boundaryError == null,
                    onAddPoint = viewModel::addPoint,
                    onMovePoint = viewModel::movePoint,
                    modifier = Modifier.fillMaxSize(),
                )
                Surface(
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ParksColors.White,
                    shadowElevation = 4.dp,
                ) {
                    Text(
                        text = if (state.points.size < 3) "Tap the map to add boundary points (at least 3)"
                        else "Drag a point to adjust the boundary",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = ParksColors.DarkGreen,
                    )
                }
            }

            BottomPanel(
                state = state,
                onUndo = viewModel::undoLastPoint,
                onClear = viewModel::clearPoints,
                onName = viewModel::onNameChange,
                onType = viewModel::onTypeChange,
                onStatus = viewModel::onStatusChange,
                onSave = viewModel::save,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomPanel(
    state: CreateParkUiState,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onName: (String) -> Unit,
    onType: (ParkType) -> Unit,
    onStatus: (ParkStatus) -> Unit,
    onSave: () -> Unit,
) {
    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = ParksColors.PrimaryGreen,
        selectedLabelColor = ParksColors.DarkGreen,
    )
    Surface(
        color = ParksColors.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 340.dp)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Points: ${state.points.size} (min 3)",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParksColors.DarkGreen,
                )
                TextButton(onClick = onUndo, enabled = state.points.isNotEmpty()) { Text("Undo") }
                TextButton(onClick = onClear, enabled = state.points.isNotEmpty()) { Text("Clear") }
            }

            state.stats?.let { stats ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Stat("Area", String.format(Locale.getDefault(), "%,.0f m²", stats.areaSqM), Modifier.weight(1f))
                    Stat("Acres", String.format(Locale.getDefault(), "%.2f", stats.areaAcres), Modifier.weight(1f))
                    Stat("Perimeter", String.format(Locale.getDefault(), "%,.0f m", stats.perimeterM), Modifier.weight(1f))
                }
            }
            state.boundaryError?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = ParksColors.ErrorRed)
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = onName,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Park name") },
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ParksColors.PrimaryGreen,
                    focusedLabelColor = ParksColors.DarkGreen,
                    cursorColor = ParksColors.DarkGreen,
                ),
            )

            Text("Type", style = MaterialTheme.typography.labelLarge, color = ParksColors.SecondaryGreen)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ParkType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { onType(type) },
                        label = { Text(type.label) },
                        colors = chipColors,
                    )
                }
            }

            Text("Status", style = MaterialTheme.typography.labelLarge, color = ParksColors.SecondaryGreen)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ParkStatus.entries.forEach { status ->
                    FilterChip(
                        selected = state.status == status,
                        onClick = { onStatus(status) },
                        label = { Text(status.label) },
                        colors = chipColors,
                    )
                }
            }

            Button(
                onClick = onSave,
                enabled = state.canSave,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParksColors.PrimaryGreen,
                    contentColor = ParksColors.DarkGreen,
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ParksColors.DarkGreen)
                } else {
                    Text("Save park")
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ParksColors.SecondaryGreen)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = ParksColors.Black,
        )
    }
}
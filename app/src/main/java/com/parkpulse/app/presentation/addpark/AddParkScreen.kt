package com.parkpulse.app.presentation.addpark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.domain.model.ParkType
import com.parkpulse.app.presentation.addpark.components.FormDateField
import com.parkpulse.app.presentation.addpark.components.FormDropdown
import com.parkpulse.app.presentation.addpark.components.ParkFieldShape
import com.parkpulse.app.presentation.addpark.components.SurveyDatePickerDialog
import com.parkpulse.app.presentation.addpark.components.parkFieldColors
import com.parkpulse.app.presentation.theme.ParksColors
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParkScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddParkViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onSaved() }
    }

    Scaffold(
        containerColor = ParksColors.BackgroundMint,
        topBar = {
            TopAppBar(
                title = { Text("Add Park", color = ParksColors.DarkGreen) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ParksColors.DarkGreen
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ParksColors.BackgroundMint)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Park name") },
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = ParkFieldShape,
                colors = parkFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            FormDropdown(
                label = "Park type",
                selected = state.type,
                options = ParkType.entries,
                optionLabel = { it.label },
                onSelect = viewModel::onTypeChange,
                error = state.typeError
            )

            OutlinedTextField(
                value = state.areaText,
                onValueChange = viewModel::onAreaChange,
                label = { Text("Area (m²)") },
                isError = state.areaError != null,
                supportingText = state.areaError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                shape = ParkFieldShape,
                colors = parkFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            FormDropdown(
                label = "Park status",
                selected = state.status,
                options = ParkStatus.entries,
                optionLabel = { it.label },
                onSelect = viewModel::onStatusChange,
                error = state.statusError
            )

            FormDateField(
                label = "Last survey date",
                valueText = state.surveyDate?.format(dateFormatter) ?: "",
                error = state.dateError,
                onClick = { showDatePicker = true }
            )

            state.saveError?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = ParksColors.ErrorRed)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::onSave,
                enabled = !state.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParksColors.DarkGreen,
                    contentColor = ParksColors.White,
                    disabledContainerColor = ParksColors.SecondaryGreen,
                    disabledContentColor = ParksColors.White
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = ParksColors.White
                    )
                } else {
                    Text("Save park")
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        SurveyDatePickerDialog(
            initial = state.surveyDate,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                viewModel.onDateChange(it)
                showDatePicker = false
            }
        )
    }
}
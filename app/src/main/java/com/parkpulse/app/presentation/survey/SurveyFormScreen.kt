package com.parkpulse.app.presentation.survey

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.domain.model.Cleanliness
import com.parkpulse.app.presentation.theme.ParksColors
import java.text.DateFormat
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyFormScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onAddPhoto: (surveyId: String) -> Unit,
    viewModel: SurveyFormViewModel, // = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // Collect safely using the exposed view model property
    val newPhotoEncoded by viewModel.newPhotoEncoded.collectAsStateWithLifecycle()

    LaunchedEffect(newPhotoEncoded) {
        newPhotoEncoded?.let { encoded ->
            viewModel.consumeNewPhoto(encoded)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                SurveyFormEvent.Saved -> onSaved()
                is SurveyFormEvent.Failed -> snackbar.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = ParksColors.BackgroundMint,
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (!state.argsValid) {
            Column(Modifier.padding(padding).padding(20.dp)) {
                Text(
                    "Check-in details are missing. Go back and check in again.",
                    color = ParksColors.ErrorRed,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ---- title row: park name ----
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = ParksColors.DarkGreen)
                }
                Text(
                    state.parkName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParksColors.DarkGreen,
                )
            }

            // ---- check-in banner ----
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip12()
                    .background(ParksColors.PrimaryGreen.copy(alpha = 0.2f))
                    .border(BorderStroke(1.5.dp, ParksColors.PrimaryGreen), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Checked in at ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(state.checkInTimeMillis))}" +
                            " · ±${state.checkInAccuracyM.roundToInt()} m",
                    style = MaterialTheme.typography.titleMedium,
                    color = ParksColors.DarkGreen,
                )
            }

            // ---- cleanliness ----
            SectionLabel("Cleanliness")
            val chipColors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = ParksColors.PrimaryGreen,
                selectedLabelColor = ParksColors.DarkGreen,
            )
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Cleanliness.entries.forEach { option ->
                    FilterChip(
                        selected = state.cleanliness == option,
                        onClick = { viewModel.onCleanlinessChange(option) },
                        label = { Text(option.label, modifier = Modifier.padding(horizontal = 8.dp)) },
                        shape = RoundedCornerShape(50),
                        colors = chipColors,
                    )
                }
            }
            state.cleanlinessError?.let { ErrorText(it) }

            // ---- photos ----
            SectionLabel("Photos (${state.photos.size})")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.photos.forEach { photo ->
                    PhotoThumb(
                        file = viewModel.photoFile(photo.fileName),
                        onRemove = { viewModel.removePhoto(photo.id) },
                    )
                }
                if (state.canAddPhoto) AddPhotoTile(onClick = { onAddPhoto(viewModel.surveyId) })
            }

            // ---- trees counted  |  Lights: Yes ----
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionLabel("Trees counted", Modifier.weight(1f))
                Text(
                    text = "Lights: ${if (state.lightsOn) "Yes" else "No"}",
                    modifier = Modifier
                        .clickable(onClick = viewModel::onLightsToggle)
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = ParksColors.DarkGreen,
                )
            }
            CountStepper(
                value = state.trees,
                onValueChange = viewModel::onTreesChange,
                onStep = viewModel::onTreesStep,
                isError = state.treesError != null,
            )
            state.treesError?.let { ErrorText(it) }

            if (state.lightsOn) {
                SectionLabel("Number of lights")
                CountStepper(
                    value = state.lights,
                    onValueChange = viewModel::onLightsChange,
                    onStep = viewModel::onLightsStep,
                    isError = state.lightsError != null,
                )
                state.lightsError?.let { ErrorText(it) }
            }

            // ---- notes ----
            SectionLabel("Notes")
            OutlinedTextField(
                value = state.notes,
                onValueChange = viewModel::onNotesChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Broken bench near gate") },
                minLines = 1,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                supportingText = { Text("${state.notes.length}/500") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ParksColors.PrimaryGreen,
                    unfocusedBorderColor = ParksColors.PrimaryGreen.copy(alpha = 0.6f),
                    cursorColor = ParksColors.DarkGreen,
                ),
            )

            // ---- submit ----
            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, ParksColors.PrimaryGreen),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ParksColors.PrimaryGreen.copy(alpha = 0.2f),
                    contentColor = ParksColors.DarkGreen,
                    disabledContainerColor = ParksColors.PrimaryGreen.copy(alpha = 0.1f),
                    disabledContentColor = ParksColors.DarkGreen,
                ),
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = ParksColors.DarkGreen)
                } else {
                    Text("Submit survey", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun Modifier.clip12(): Modifier = this.then(Modifier.clip(RoundedCornerShape(12.dp)))

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = ParksColors.DarkGreen,
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = ParksColors.ErrorRed)
}

/** "−  42  +" stepper from the design; the centre is also directly editable for large counts. */
@Composable
private fun CountStepper(
    value: String,
    onValueChange: (String) -> Unit,
    onStep: (Int) -> Unit,
    isError: Boolean,
) {
    val borderColor = if (isError) ParksColors.ErrorRed else ParksColors.PrimaryGreen
    val shape = RoundedCornerShape(14.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(ParksColors.White, shape)
            .border(1.5.dp, borderColor, shape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepButton("−", onClick = { onStep(-1) })
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = ParksColors.Black,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(ParksColors.DarkGreen),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.Center) {
                    if (value.isEmpty()) {
                        Text("0", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = ParksColors.SecondaryGreen)
                    }
                    inner()
                }
            },
        )
        StepButton("+", onClick = { onStep(+1) })
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier
            .width(64.dp)
            .fillMaxSize()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, fontSize = 26.sp, fontWeight = FontWeight.Medium, color = ParksColors.DarkGreen)
    }
}

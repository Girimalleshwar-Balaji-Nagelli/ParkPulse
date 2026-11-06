package com.parkpulse.app.presentation.addpark.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.parkpulse.app.presentation.theme.ParksColors
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun parkFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ParksColors.White,
    unfocusedContainerColor = ParksColors.White,
    errorContainerColor = ParksColors.White,
    focusedBorderColor = ParksColors.DarkGreen,
    unfocusedBorderColor = ParksColors.SecondaryGreen,
    errorBorderColor = ParksColors.ErrorRed,
    focusedLabelColor = ParksColors.DarkGreen,
    unfocusedLabelColor = ParksColors.SecondaryGreen,
    errorLabelColor = ParksColors.ErrorRed,
    cursorColor = ParksColors.DarkGreen,
    errorCursorColor = ParksColors.ErrorRed,
    errorSupportingTextColor = ParksColors.ErrorRed,
    focusedTextColor = ParksColors.Black,
    unfocusedTextColor = ParksColors.Black,
    errorTextColor = ParksColors.Black,
    focusedTrailingIconColor = ParksColors.DarkGreen,
    unfocusedTrailingIconColor = ParksColors.DarkGreen,
    errorTrailingIconColor = ParksColors.ErrorRed
)

val ParkFieldShape = RoundedCornerShape(16.dp)

/** Read-only field that behaves like a button (used for dropdowns and the date picker). */
@Composable
private fun ClickableField(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    overlay: @Composable () -> Unit = {}
) {
    val focus: FocusManager = LocalFocusManager.current
    Box(modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = trailing,
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            shape = ParkFieldShape,
            colors = parkFieldColors(),
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focus.clearFocus()
                    onClick()
                }
        )
        overlay()
    }
}

@Composable
fun <T> FormDropdown(
    label: String,
    selected: T?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    error: String?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ClickableField(
        label = label,
        value = selected?.let(optionLabel) ?: "",
        error = error,
        onClick = { expanded = true },
        trailing = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        modifier = modifier
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(ParksColors.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), color = ParksColors.Black) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FormDateField(
    label: String,
    valueText: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ClickableField(
        label = label,
        value = valueText,
        error = error,
        onClick = onClick,
        trailing = { Icon(Icons.Default.DateRange, contentDescription = "Pick date") },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyDatePickerDialog(
    initial: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    // The picker works in UTC midnight millis; "today" is the device's local date.
    val todayMillis = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis <= todayMillis
        }
    )
    val colors = DatePickerDefaults.colors(
        containerColor = ParksColors.White,
        selectedDayContainerColor = ParksColors.DarkGreen,
        selectedDayContentColor = ParksColors.White,
        todayContentColor = ParksColors.DarkGreen,
        todayDateBorderColor = ParksColors.DarkGreen
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onConfirm(java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                }
            ) { Text("OK", color = ParksColors.DarkGreen) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ParksColors.SecondaryGreen) }
        }
    ) {
        DatePicker(state = pickerState, colors = colors)
    }
}
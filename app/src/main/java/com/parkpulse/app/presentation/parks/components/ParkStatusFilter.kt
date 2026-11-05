package com.parkpulse.app.presentation.parks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.parkpulse.app.presentation.theme.ParksColors
import com.parkpulse.app.presentation.parks.StatusFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkStatusFilter(
    selected: StatusFilter,
    onSelected: (StatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        items(StatusFilter.entries) { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelected(option) },
                label = { Text(option.label) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = ParksColors.White,
                    labelColor = ParksColors.DarkGreen,
                    selectedContainerColor = ParksColors.PrimaryGreen,
                    selectedLabelColor = ParksColors.Black
                )
            )
        }
    }
}
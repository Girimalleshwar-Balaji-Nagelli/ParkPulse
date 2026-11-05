package com.parkpulse.app.presentation.parks.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.presentation.theme.ParksColors

@Composable
fun ParkStatusBadge(status: ParkStatus, modifier: Modifier = Modifier) {
    val color = when (status) {
        ParkStatus.OPEN -> ParksColors.StatusOpen
        ParkStatus.NEEDS_REPAIR -> ParksColors.StatusRepair
        ParkStatus.CLOSED -> ParksColors.StatusClosed
    }
    Badge(text = status.label, color = color, modifier = modifier)
}

@Composable
fun PendingSyncBadge(modifier: Modifier = Modifier) {
    Badge(text = "Pending Sync", color = ParksColors.DarkGreen, modifier = modifier)
}

@Composable
private fun Badge(text: String, color: Color, modifier: Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
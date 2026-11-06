package com.parkpulse.app.presentation.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parkpulse.app.gis.DistanceCalculator
import com.parkpulse.app.gis.PolygonUtils
import com.parkpulse.app.domain.model.ParkStatus
import com.parkpulse.app.presentation.map.SelectedParkInfo
import com.parkpulse.app.presentation.theme.ParksColors
import kotlin.math.roundToInt

@Composable
internal fun ParkInfoSheetContent(info: SelectedParkInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = info.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = ParksColors.DarkGreen,
        )
        StatusRow(info.status)
        HorizontalDivider(color = ParksColors.SecondaryGreen)

        InfoRow("Area", PolygonUtils.formatArea(info.areaSquareMeters))

        val distanceText = when {
            info.isUserInside -> "You are inside this park"
            info.distanceMeters != null -> DistanceCalculator.format(info.distanceMeters) + " to nearest boundary"
            else -> "Location unavailable"
        }
        InfoRow("Distance", distanceText)

        if (info.direction != null && info.bearingDegrees != null) {
            InfoRow("Direction", "${info.direction.label} (${info.bearingDegrees.roundToInt()}°)")
        }
    }
}

@Composable
private fun StatusRow(status: ParkStatus) {
    val (label, color) = when (status) {
        ParkStatus.OPEN -> status.label to ParksColors.StatusOpen
        ParkStatus.NEEDS_REPAIR -> status.label to ParksColors.StatusRepair
        ParkStatus.CLOSED -> status.label to ParksColors.StatusClosed
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, color = ParksColors.Black)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = ParksColors.SecondaryGreen,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = ParksColors.Black,
        )
    }
}
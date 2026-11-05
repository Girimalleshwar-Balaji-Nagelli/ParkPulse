package com.parkpulse.app.presentation.parks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.parkpulse.app.domain.model.Park
import com.parkpulse.app.presentation.theme.ParksColors
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
fun ParkCard(park: Park, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ParksColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = park.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = ParksColors.DarkGreen,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                ParkStatusBadge(park.status)
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = park.type.label,
                style = MaterialTheme.typography.bodyMedium,
                color = ParksColors.SecondaryGreen
            )

            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoColumn("Area", "%,.0f m²".format(park.areaSqM))
                InfoColumn("Last survey", park.lastSurveyDate.format(dateFormatter))
            }

            if (park.isPendingSync) {
                Spacer(Modifier.height(12.dp))
                PendingSyncBadge()
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = ParksColors.SecondaryGreen)
        Text(value, style = MaterialTheme.typography.bodyMedium, color = ParksColors.Black)
    }
}
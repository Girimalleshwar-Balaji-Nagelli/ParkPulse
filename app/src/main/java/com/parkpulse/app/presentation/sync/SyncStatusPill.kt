package com.parkpulse.app.presentation.sync

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.data.sync.SyncState
import com.parkpulse.app.presentation.theme.ParksColors

private data class PillContent(val icon: ImageVector, val tint: Color, val text: String)

private fun describe(ui: SyncUiState): PillContent {
    val s = ui.state
    return when {
        s is SyncState.Running ->
            PillContent(Icons.Filled.Refresh, ParksColors.PrimaryGreen, "Syncing ${s.done} of ${s.total}…")
        s is SyncState.Failed ->
            PillContent(Icons.Filled.Warning, ParksColors.ErrorRed, "Sync failed: ${s.message} (${s.remaining} pending)")
        !ui.online ->
            PillContent(
                Icons.Filled.Info, ParksColors.SecondaryGreen,
                if (ui.pending > 0) "Offline · ${ui.pending} pending, saved on device" else "Offline · all data saved",
            )
        ui.pending > 0 ->
            PillContent(Icons.Filled.Refresh, ParksColors.StatusRepair, "${ui.pending} pending sync")
        s is SyncState.Success && s.synced > 0 ->
            PillContent(Icons.Filled.Check, ParksColors.StatusOpen, "Synced ${s.synced} item(s)")
        else ->
            PillContent(Icons.Filled.Check, ParksColors.StatusOpen, "All changes synced")
    }
}

/** Compact status + "Sync now" control. Long-press arms a one-off mock-server failure (testing). */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SyncStatusPill(
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = hiltViewModel(),
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val running = ui.state is SyncState.Running
    val failed = ui.state is SyncState.Failed
    val content = describe(ui)

    Surface(
        modifier = modifier.combinedClickable(onClick = {}, onLongClick = viewModel::armTestFailure),
        shape = RoundedCornerShape(24.dp),
        color = ParksColors.White,
        shadowElevation = 6.dp,
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            if (running) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = ParksColors.PrimaryGreen)
            } else {
                Icon(content.icon, contentDescription = null, tint = content.tint, modifier = Modifier.size(18.dp))
            }
            Text(
                text = content.text + if (ui.testFailureArmed) " [test failure armed]" else "",
                modifier = Modifier.widthIn(max = 220.dp),
                style = MaterialTheme.typography.labelMedium,
                color = ParksColors.DarkGreen,
                maxLines = 3,
            )
            TextButton(onClick = viewModel::syncNow, enabled = !running) {
                Text(if (failed) "Retry" else "Sync now")
            }
        }
    }
}
package com.parkpulse.app.presentation.survey

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.gis.CheckInResult
import com.parkpulse.app.gis.CheckInValidator
import com.parkpulse.app.gis.DistanceCalculator
import com.parkpulse.app.presentation.theme.ParksColors
import kotlin.math.roundToInt

private data class StatusInfo(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val detail: String,
)

private fun describe(status: CheckInStatus, parkName: String): StatusInfo = when (status) {
    CheckInStatus.Loading ->
        StatusInfo(Icons.Filled.Info, ParksColors.SecondaryGreen, "Loading park…", "")
    CheckInStatus.ParkNotFound ->
        StatusInfo(Icons.Filled.Warning, ParksColors.ErrorRed, "Park not found", "This park no longer exists. Go back and choose another.")
    CheckInStatus.NoBoundary ->
        StatusInfo(
            Icons.Filled.Warning, ParksColors.ErrorRed, "No verified boundary",
            "This park has no verified boundary yet, so your location cannot be checked against it.",
        )
    CheckInStatus.PermissionDenied ->
        StatusInfo(
            Icons.Filled.Warning, ParksColors.ErrorRed, "Location permission denied",
            "Allow location access so the app can confirm you are inside the park.",
        )
    CheckInStatus.LocationOff ->
        StatusInfo(Icons.Filled.Warning, ParksColors.ErrorRed, "Location is turned off", "Turn on device location to check in.")
    CheckInStatus.SearchingGps ->
        StatusInfo(Icons.Filled.Place, ParksColors.SecondaryGreen, "Searching for GPS…", "Stay outdoors and wait a few seconds.")
    is CheckInStatus.Evaluated -> when (val r = status.result) {
        is CheckInResult.Allowed ->
            StatusInfo(
                Icons.Filled.Check, ParksColors.StatusOpen, "You are inside $parkName",
                "GPS accuracy ±${r.accuracyM.roundToInt()} m. You can check in.",
            )
        is CheckInResult.PoorAccuracy ->
            StatusInfo(
                Icons.Filled.Warning, ParksColors.StatusRepair, "GPS accuracy too low",
                if (r.accuracyM == null) "No accuracy reading yet. Move to open sky and wait."
                else "Accuracy is ±${r.accuracyM.roundToInt()} m; ${CheckInValidator.MAX_ACCURACY_M.roundToInt()} m or better is required. Move to open sky and wait.",
            )
        is CheckInResult.Outside ->
            StatusInfo(
                Icons.Filled.Warning, ParksColors.ErrorRed, "You are outside $parkName",
                "The nearest boundary is ${DistanceCalculator.format(r.distanceM)} to the ${r.direction.label}. Walk inside the park to check in.",
            )
        CheckInResult.StaleFix ->
            StatusInfo(Icons.Filled.Place, ParksColors.SecondaryGreen, "Waiting for a fresh GPS fix…", "The last reading is too old to trust.")
        CheckInResult.MockLocation ->
            StatusInfo(
                Icons.Filled.Warning, ParksColors.ErrorRed, "Mock location detected",
                "Turn off mock or fake location apps to check in.",
            )
        CheckInResult.NoBoundary ->
            StatusInfo(Icons.Filled.Warning, ParksColors.ErrorRed, "No verified boundary", "This park has no verified boundary yet.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBack: () -> Unit,
    onCheckedIn: (CheckInEvent.CheckedIn) -> Unit,
    viewModel: CheckInViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var askedForPermission by rememberSaveable { mutableStateOf(false) }

    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        viewModel.onPermissionResult(
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CheckInEvent.CheckedIn -> onCheckedIn(event)
            }
        }
    }

    // Ask once automatically; afterwards the user uses the buttons below.
    LaunchedEffect(state.status) {
        if (state.status == CheckInStatus.PermissionDenied && !askedForPermission) {
            askedForPermission = true
            permissionLauncher.launch(permissions)
        }
    }

    // GPS runs only while the screen is visible.
    LifecycleStartEffect(Unit) {
        viewModel.onScreenStart()
        onStopOrDispose { viewModel.onScreenStop() }
    }

    val info = describe(state.status, state.parkName)

    Scaffold(
        containerColor = ParksColors.BackgroundMint,
        topBar = {
            TopAppBar(
                title = { Text("Check in") },
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
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (state.parkName.isNotEmpty()) {
                Text(
                    state.parkName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ParksColors.DarkGreen,
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ParksColors.White),
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(info.icon, contentDescription = null, tint = info.tint, modifier = Modifier.size(28.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(info.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ParksColors.DarkGreen)
                        if (info.detail.isNotEmpty()) {
                            Text(info.detail, style = MaterialTheme.typography.bodyMedium, color = ParksColors.Black)
                        }
                    }
                }
            }

            Text(
                "To check in you must be inside the park boundary with GPS accuracy of " +
                    "${CheckInValidator.MAX_ACCURACY_M.roundToInt()} m or better.",
                style = MaterialTheme.typography.bodySmall,
                color = ParksColors.SecondaryGreen,
            )

            when (state.status) {
                CheckInStatus.PermissionDenied -> {
                    Button(
                        onClick = { permissionLauncher.launch(permissions) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ParksColors.PrimaryGreen, contentColor = ParksColors.DarkGreen),
                    ) { Text("Allow location") }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open app settings") }
                }
                CheckInStatus.LocationOff -> {
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open location settings") }
                }
                else -> Unit
            }

            Button(
                onClick = viewModel::onCheckIn,
                enabled = state.canCheckIn,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ParksColors.PrimaryGreen, contentColor = ParksColors.DarkGreen),
            ) { Text("Check in and start survey") }
        }
    }
}

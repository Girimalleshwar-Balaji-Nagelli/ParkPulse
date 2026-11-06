package com.parkpulse.app.presentation.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.parkpulse.app.presentation.components.BottomTab
import com.parkpulse.app.presentation.components.ParkPulseBottomBar
import com.parkpulse.app.presentation.map.components.ParkInfoSheetContent
import com.parkpulse.app.presentation.map.components.ParkMapView
import com.parkpulse.app.presentation.map.components.rememberMapController
import com.parkpulse.app.presentation.theme.ParksColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onListClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val controller = rememberMapController()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        viewModel.onPermissionResult(
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        )
    }

    LaunchedEffect(Unit) {
        if (!state.hasLocationPermission) permissionLauncher.launch(permissions)
    }

    // GPS only runs while the screen is visible.
    LifecycleStartEffect(Unit) {
        viewModel.startLocationUpdates()
        onStopOrDispose { viewModel.stopLocationUpdates() }
    }

    Scaffold(
        modifier = modifier,
        containerColor = ParksColors.BackgroundMint,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            ParkPulseBottomBar(
                selected = BottomTab.MAP,
                onListClick = onListClick,
                onMapClick = {},
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            ParkMapView(
                controller = controller,
                parks = state.parks,
                selectedParkId = state.selectedParkId,
                userLocation = state.userLocation,
                hasLocationPermission = state.hasLocationPermission,
                onParkTapped = viewModel::onParkTapped,
                modifier = Modifier.fillMaxSize(),
            )

            if (state.missingBoundaryCount > 0 || state.invalidBoundaryCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .statusBarsPadding()
                        .padding(12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = ParksColors.White,
                    shadowElevation = 4.dp,
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        if (state.missingBoundaryCount > 0) {
                            Text(
                                text = "${state.missingBoundaryCount} park(s) have no verified boundary yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParksColors.DarkGreen,
                            )
                        }
                        if (state.invalidBoundaryCount > 0) {
                            Text(
                                text = "${state.invalidBoundaryCount} park(s) have an invalid boundary",
                                style = MaterialTheme.typography.bodySmall,
                                color = ParksColors.DarkGreen,
                            )
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    val loc = state.userLocation
                    when {
                        !state.hasLocationPermission -> permissionLauncher.launch(permissions)
                        loc == null -> scope.launch {
                            val msg = if (viewModel.isLocationServiceEnabled()) "Waiting for GPS fix…"
                            else "Turn on device location to see where you are"
                            snackbar.showSnackbar(msg)
                        }
                        else -> controller.moveTo(loc)
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                containerColor = ParksColors.White,
                contentColor = ParksColors.PrimaryGreen,
            ) {
                Icon(Icons.Filled.Place, contentDescription = "My location")
            }

            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
        }
    }

    state.selected?.let { info ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onParkTapped(null) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ParksColors.White,
        ) {
            ParkInfoSheetContent(info)
        }
    }
}
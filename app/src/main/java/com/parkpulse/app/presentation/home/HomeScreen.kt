package com.parkpulse.app.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.parkpulse.app.presentation.components.BottomTab
import com.parkpulse.app.presentation.components.ParkPulseBottomBar

/** Temporary placeholder. Replaced when the real Home feature is built. */
@Composable
fun HomeScreen(
//    onLoggedOut: () -> Unit,
    onLoggedOut:() -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    Scaffold(
        bottomBar = {
            ParkPulseBottomBar(
                selected = BottomTab.MAP,
//                onListClick = onListClick,
                onListClick = onLoggedOut,
                onMapClick = {}
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .safeDrawingPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome to ParkPulse",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text("Temporary Home Screen", style = MaterialTheme.typography.bodyLarge)
            Button(
                onClick = { viewModel.logout(onLoggedOut) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Logout") }
        }
    }

}

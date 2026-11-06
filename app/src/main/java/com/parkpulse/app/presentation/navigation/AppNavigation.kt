package com.parkpulse.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parkpulse.app.domain.repository.AuthRepository
import com.parkpulse.app.presentation.addpark.AddParkScreen
import com.parkpulse.app.presentation.auth.LoginScreen
import com.parkpulse.app.presentation.home.HomeScreen
import com.parkpulse.app.presentation.map.MapScreen
import com.parkpulse.app.presentation.parks.ParksScreen
import com.parkpulse.app.presentation.parks.ParksViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PARKS = "parks"
    const val MAP = "map"
    const val ADD_PARK = "add_park"
}

private const val KEY_PARK_ADDED = "park_added"

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    /** Reads the persisted session once to pick the start destination. */
    suspend fun startDestination(): String =
        if (authRepository.isLoggedIn.first()) Routes.PARKS else Routes.LOGIN

    /** Clears the session, then runs [onDone] (used to navigate back to login). */
    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onDone()
        }
    }
}

@Composable
fun AppNavigation(viewModel: AppViewModel = hiltViewModel()) {
    val start by produceState<String?>(initialValue = null) {
        value = viewModel.startDestination()
    }

    val startDestination = start
    if (startDestination == null) {
        // Brief blank frame while DataStore is read.
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoginSuccess = {
                navController.navigate(Routes.PARKS) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.PARKS) { entry ->
            val parksViewModel: ParksViewModel = hiltViewModel()

            // After a successful save, reset search/filter so the new park is visible.
            val parkAdded by entry.savedStateHandle
                .getStateFlow(KEY_PARK_ADDED, false)
                .collectAsStateWithLifecycle()
            LaunchedEffect(parkAdded) {
                if (parkAdded) {
                    parksViewModel.clearFilters()
                    entry.savedStateHandle[KEY_PARK_ADDED] = false
                }
            }

            ParksScreen(
                viewModel = parksViewModel,
                onProfileClick = {
                    viewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.PARKS) { inclusive = true }
                        }
                    }
                },
                onMapClick = {
                    navController.navigate(Routes.MAP) { launchSingleTop = true }
                },
                onAddParkClick = {
                    navController.navigate(Routes.ADD_PARK) { launchSingleTop = true }
                }
            )
        }
        composable(Routes.MAP) {
            MapScreen(
                onListClick = { navController.popBackStack(Routes.PARKS, inclusive = false) }
            )
        }
        composable(Routes.ADD_PARK) {
            AddParkScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KEY_PARK_ADDED, true)
                    navController.popBackStack()
                }
            )        }
    }
}

package com.parkpulse.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parkpulse.app.domain.repository.AuthRepository
import com.parkpulse.app.presentation.auth.LoginScreen
import com.parkpulse.app.presentation.home.HomeScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
}

@HiltViewModel
class AppViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    /** Reads the persisted session once to pick the start destination. */
    suspend fun startDestination(): String =
        if (authRepository.isLoggedIn.first()) Routes.HOME else Routes.LOGIN
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
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            })
        }
        composable(Routes.HOME) {
            HomeScreen(onLoggedOut = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.HOME) { inclusive = true }
                }
            })
        }
    }
}

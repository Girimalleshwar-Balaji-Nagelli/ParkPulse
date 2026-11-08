package com.parkpulse.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.parkpulse.app.domain.repository.AuthRepository
import com.parkpulse.app.presentation.auth.LoginScreen
import com.parkpulse.app.presentation.camera.CameraScreen
import com.parkpulse.app.presentation.createpark.CreateParkScreen
import com.parkpulse.app.presentation.home.HomeScreen
import com.parkpulse.app.presentation.map.MapScreen
import com.parkpulse.app.presentation.parks.ParksScreen
import com.parkpulse.app.presentation.parks.ParksViewModel
import com.parkpulse.app.presentation.survey.CheckInScreen
import com.parkpulse.app.presentation.survey.SurveyFormScreen
import com.parkpulse.app.presentation.survey.SurveyFormViewModel
import com.parkpulse.app.presentation.survey.SurveyParkListScreen
import com.parkpulse.app.presentation.sync.SyncStatusPill
import com.parkpulse.app.presentation.theme.ParksColors
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

    const val SURVEY_SELECT = "survey_select"
    const val CHECK_IN = "check_in/{parkId}"
    const val SURVEY_FORM = "survey_form/{parkId}/{checkInTime}/{lat}/{lng}/{accuracy}"
    const val CAMERA = "camera/{parkId}/{surveyId}"

    fun checkIn(parkId: String) = "check_in/$parkId"

    fun camera(parkId: String, surveyId: String) = "camera/$parkId/$surveyId"

    fun surveyForm(parkId: String, checkInTime: Long, lat: Double, lng: Double, accuracyM: Float) =
        "survey_form/$parkId/$checkInTime/$lat/$lng/$accuracyM"
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
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showOverlay = currentRoute == Routes.PARKS || currentRoute == Routes.MAP

    Box(Modifier.fillMaxSize()) {
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
                CreateParkScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(KEY_PARK_ADDED, true)
                        navController.popBackStack()
                    }
                )
            }

            // ---- Survey flow: select park -> check in (geofence + GPS accuracy) -> survey form ----
            composable(Routes.SURVEY_SELECT) {
                SurveyParkListScreen(
                    onBack = { navController.popBackStack() },
                    onParkSelected = { parkId ->
                        navController.navigate(Routes.checkIn(parkId)) { launchSingleTop = true }
                    }
                )
            }
            composable(
                route = Routes.CHECK_IN,
                arguments = listOf(navArgument("parkId") { type = NavType.StringType }),
            ) {
                CheckInScreen(
                    onBack = { navController.popBackStack() },
                    onCheckedIn = { e ->
                        navController.navigate(
                            Routes.surveyForm(e.parkId, e.timeMillis, e.latitude, e.longitude, e.accuracyM)
                        ) {
                            // Back from the form returns to park selection, not to a stale check-in.
                            popUpTo(Routes.CHECK_IN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = Routes.SURVEY_FORM,
                arguments = listOf(
                    navArgument("parkId") { type = NavType.StringType },
                    navArgument("checkInTime") { type = NavType.LongType },
                    navArgument("lat") { type = NavType.StringType },
                    navArgument("lng") { type = NavType.StringType },
                    navArgument("accuracy") { type = NavType.StringType },
                ),
            ) { entry ->
                val parkId = entry.arguments?.getString("parkId").orEmpty()

                // Explicitly scope the ViewModel to this specific NavBackStackEntry
                // Fix: Scope the ViewModel to the NavHost graph directly so it survives the camera transition perfectly
                val surveyViewModel: SurveyFormViewModel = hiltViewModel(
                    viewModelStoreOwner = entry
                )

                SurveyFormScreen(
                    viewModel = surveyViewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack(Routes.PARKS, inclusive = false) },
                    onAddPhoto = { surveyId -> navController.navigate(Routes.camera(parkId, surveyId)) { launchSingleTop = true } },
                )
            }
            composable(
                route = Routes.CAMERA,
                arguments = listOf(
                    navArgument("parkId") { type = NavType.StringType },
                    navArgument("surveyId") { type = NavType.StringType },
                ),
            ) {
                CameraScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onPhotoConfirmed = { encodedDraft ->
                        // Look up the exact survey form destination anywhere in the backstack
                        navController.getBackStackEntry(Routes.SURVEY_FORM).savedStateHandle.set(
                            SurveyFormViewModel.KEY_NEW_PHOTO,
                            encodedDraft
                        )

                        navController.popBackStack()
                    }
                )
            }
        }

        if (showOverlay) {
            // 88.dp keeps these above the bottom navigation bar; adjust if your bar height differs.
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 88.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { navController.navigate(Routes.SURVEY_SELECT) { launchSingleTop = true } },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ParksColors.PrimaryGreen,
                        contentColor = ParksColors.DarkGreen,
                    ),
                ) { Text("Start survey") }
                SyncStatusPill()
            }
        }
    }
}

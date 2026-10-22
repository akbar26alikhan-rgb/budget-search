package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.components.AppBottomNav
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CompareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapViewScreen
import com.example.ui.screens.MealBuilderScreen
import com.example.ui.screens.RestaurantDetailScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.screens.SearchResultsScreen
import com.example.ui.theme.BudgetBitesTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BudgetBitesTheme {
                BudgetBitesApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BudgetBitesApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allRestaurants by viewModel.repository.getAllRestaurants().collectAsStateWithLifecycle(initialValue = emptyList())
    val selectedRestaurantId by viewModel.selectedRestaurantId.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val showGoogleApiDialog by viewModel.showGoogleApiDialog.collectAsStateWithLifecycle()

    if (showGoogleApiDialog) {
        com.example.ui.components.GooglePlacesConfigDialog(
            apiKeyManager = viewModel.apiKeyManager,
            placesService = viewModel.placesService,
            viewModel = viewModel,
            onDismiss = { viewModel.showGoogleApiDialog.value = false }
        )
    }

    val selectedRestaurant = allRestaurants.firstOrNull { it.id == selectedRestaurantId }
    val categories by viewModel.repository.getCategories(selectedRestaurantId ?: "").collectAsStateWithLifecycle(initialValue = emptyList())
    val menuItems by viewModel.repository.getMenuItems(selectedRestaurantId ?: "").collectAsStateWithLifecycle(initialValue = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNav(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    restaurants = allRestaurants
                )

                Screen.SEARCH_RESULTS -> SearchResultsScreen(
                    viewModel = viewModel,
                    restaurants = allRestaurants
                )

                Screen.RESTAURANT_DETAIL -> RestaurantDetailScreen(
                    restaurant = selectedRestaurant,
                    categories = categories,
                    menuItems = menuItems,
                    viewModel = viewModel
                )

                Screen.MEAL_BUILDER -> MealBuilderScreen(
                    viewModel = viewModel,
                    categories = categories,
                    menuItems = menuItems
                )

                Screen.COMPARE -> CompareScreen(
                    viewModel = viewModel,
                    allRestaurants = allRestaurants
                )

                Screen.MAP_VIEW -> MapViewScreen(
                    viewModel = viewModel,
                    restaurants = allRestaurants
                )

                Screen.SAVED_PLANS -> SavedScreen(
                    viewModel = viewModel,
                    allRestaurants = allRestaurants
                )

                Screen.ADMIN_PORTAL -> AdminScreen(
                    viewModel = viewModel,
                    restaurants = allRestaurants
                )
            }
        }
    }
}

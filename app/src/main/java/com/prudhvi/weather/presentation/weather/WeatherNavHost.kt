package com.prudhvi.weather.presentation.weather

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val WEATHER_ROUTE = "weather"

@Composable
fun WeatherNavHost(viewModel: WeatherViewModel) {
    val navController = rememberNavController()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = WEATHER_ROUTE
        ) {
            composable(WEATHER_ROUTE) {
                WeatherScreen(
                    uiState = uiState,
                    onSearch = viewModel::search
                )
            }
        }
    }
}

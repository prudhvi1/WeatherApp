package com.prudhvi.weather

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.prudhvi.weather.presentation.weather.WeatherNavHost
import com.prudhvi.weather.presentation.weather.WeatherViewModel
import com.prudhvi.weather.presentation.weather.WeatherViewModelFactory

class MainActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels {
        val container = (application as WeatherApplication).appContainer
        WeatherViewModelFactory(
            repository = container.weatherRepository,
            weatherPreferences = container.weatherPreferences,
            locationProvider = container.locationProvider
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "OpenWeather API key configured: " +
                    "${BuildConfig.OPENWEATHER_API_KEY.isNotBlank()}, " +
                    "length: ${BuildConfig.OPENWEATHER_API_KEY.length}"
            )
        }

        setContent {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.LightGray,
                            titleContentColor = Color.Black
                        ),
                        title = {
                            Text(
                                text = "Weather",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    WeatherNavHost(viewModel = weatherViewModel)
                }
            }
        }
    }

    private companion object {
        const val TAG = "WeatherConfig"
    }
}

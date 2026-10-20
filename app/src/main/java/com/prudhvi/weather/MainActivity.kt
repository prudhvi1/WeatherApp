package com.prudhvi.weather

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
            WeatherNavHost(viewModel = weatherViewModel)
        }
    }

    private companion object {
        const val TAG = "WeatherConfig"
    }
}

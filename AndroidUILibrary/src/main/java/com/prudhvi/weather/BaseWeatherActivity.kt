package com.prudhvi.weather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.prudhvi.weather.presentation.weather.WeatherNavHost
import com.prudhvi.weather.presentation.weather.WeatherViewModel
import com.prudhvi.weather.presentation.weather.WeatherViewModelFactory

/** Shared activity wiring for white-label weather applications. */
open class BaseWeatherActivity : ComponentActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels {
        val container = (application as BaseWeatherApplication).appContainer
        WeatherViewModelFactory(
            repository = container.weatherRepository,
            weatherPreferences = container.weatherPreferences,
            locationProvider = container.locationProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WeatherNavHost(viewModel = weatherViewModel) }
    }
}

package com.prudhvi.weather.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.LocationProvider

class WeatherViewModelFactory(
    private val repository: WeatherRepository,
    private val weatherPreferences: WeatherPreferences,
    private val locationProvider: LocationProvider
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(
                repository = repository,
                weatherPreferences = weatherPreferences,
                locationProvider = locationProvider
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

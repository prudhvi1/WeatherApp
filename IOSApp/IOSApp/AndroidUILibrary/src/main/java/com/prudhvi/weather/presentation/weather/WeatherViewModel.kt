package com.prudhvi.weather.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.CurrentLocationProvider
import com.prudhvi.weather.data.location.LocationPermissionException
import com.prudhvi.weather.data.location.LocationUnavailableException
import com.prudhvi.weather.shared.models.Coordinates
import com.prudhvi.weather.shared.models.WeatherResult
import com.prudhvi.weather.shared.models.WeatherSnapshot
import com.prudhvi.weather.shared.weather.GetWeatherForCity
import com.prudhvi.weather.shared.weather.GetWeatherForCurrentLocation
import com.prudhvi.weather.shared.weather.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val city: String,
        val weather: WeatherSnapshot
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val weatherPreferences: WeatherPreferences,
    private val locationProvider: CurrentLocationProvider
) : ViewModel() {
    private val getWeatherForCity = GetWeatherForCity(repository)
    private val getWeatherForCurrentLocation = GetWeatherForCurrentLocation(repository)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            search(weatherPreferences.lastCity.first() ?: DEFAULT_CITY)
        }
    }

    fun search(city: String = DEFAULT_CITY) {
        val normalizedCity = city.trim()
        if (normalizedCity.isEmpty()) {
            _uiState.value = WeatherUiState.Error("Enter a city name")
            return
        }

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            when (val result = getWeatherForCity(normalizedCity)) {
                is WeatherResult.Success -> {
                    weatherPreferences.saveLastCity(result.snapshot.city)
                    _uiState.value = WeatherUiState.Success(
                        city = result.snapshot.city,
                        weather = result.snapshot
                    )
                }
                is WeatherResult.Failure -> _uiState.value = WeatherUiState.Error(
                    result.message.toUserMessage()
                )
            }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val location = locationProvider.getCurrentLocation()
                when (val result = getWeatherForCurrentLocation(
                    Coordinates(location.latitude, location.longitude)
                )) {
                    is WeatherResult.Success -> {
                        weatherPreferences.saveLastCity(result.snapshot.city)
                        _uiState.value = WeatherUiState.Success(
                            city = result.snapshot.city,
                            weather = result.snapshot
                        )
                    }
                    is WeatherResult.Failure -> _uiState.value = WeatherUiState.Error(
                        result.message.toUserMessage()
                    )
                }
            } catch (exception: LocationPermissionException) {
                _uiState.value = WeatherUiState.Error(
                    "Location permission is required to fetch current weather"
                )
            } catch (exception: LocationUnavailableException) {
                _uiState.value = WeatherUiState.Error(
                    "Turn on location services and try again"
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.value = WeatherUiState.Error(message)
    }

    private fun String.toUserMessage(): String = when {
        contains("cannot be empty", ignoreCase = true) -> "Enter a city name"
        contains("could not find location", ignoreCase = true) ||
            contains("no weather location found", ignoreCase = true) -> "We could not find that city"
        contains("401") -> "The OpenWeather API key is invalid or not active yet"
        contains("429") -> "The weather service request limit was reached"
        contains("failed to fetch", ignoreCase = true) ->
            "Check your internet connection and try again"
        else -> this.ifBlank { "Something went wrong. Please try again" }
    }

    private companion object {
        const val DEFAULT_CITY = "Chicago"
    }
}

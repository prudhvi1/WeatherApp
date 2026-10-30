package com.prudhvi.weather.presentation.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudhvi.weather.data.CityNotFoundException
import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.LocationPermissionException
import com.prudhvi.weather.data.location.CurrentLocationProvider
import com.prudhvi.weather.data.location.LocationUnavailableException
import com.prudhvi.weather.data.remote.model.WeatherResponse
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val city: String,
        val weather: WeatherResponse,
        val iconUrl: String?
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
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // A newer search or location request supersedes the older one.
    private var weatherJob: Job? = null

    init {
        // Restore the last searched city; use the default on a fresh install.
        weatherJob = viewModelScope.launch {
            val initialCity = weatherPreferences.lastCity.first() ?: DEFAULT_CITY
            loadWeatherForCity(initialCity)
        }
    }

    fun search(city: String = DEFAULT_CITY) {
        val normalizedCity = city.trim()
        weatherJob?.cancel()
        if (normalizedCity.isEmpty()) {
            weatherJob = null
            _uiState.value = WeatherUiState.Error("Enter a city name")
            return
        }

        weatherJob = viewModelScope.launch {
            loadWeatherForCity(normalizedCity)
        }
    }

    fun fetchCurrentLocation() {
        weatherJob?.cancel()
        weatherJob = viewModelScope.launch {
            loadWeatherForCurrentLocation()
        }
    }

    private suspend fun loadWeatherForCity(city: String) {
        _uiState.value = WeatherUiState.Loading

        try {
            val weather = repository.getWeatherForCity(city)
            weatherPreferences.saveLastCity(city)
            _uiState.value = successState(city, weather)
        } catch (exception: CityNotFoundException) {
            _uiState.value = WeatherUiState.Error("We could not find that city")
        } catch (exception: IOException) {
            _uiState.value = WeatherUiState.Error(
                "Check your internet connection and try again"
            )
        } catch (exception: HttpException) {
            val message = when (exception.code()) {
                401 -> "The OpenWeather API key is invalid or not active yet"
                429 -> "The weather service request limit was reached"
                else -> "Weather service returned an error (${exception.code()})"
            }
            _uiState.value = WeatherUiState.Error(message)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.value = WeatherUiState.Error(
                "Something went wrong. Please try again"
            )
        }
    }

    private suspend fun loadWeatherForCurrentLocation() {
        _uiState.value = WeatherUiState.Loading

        try {
            val location = locationProvider.getCurrentLocation()
            val weather = repository.getWeatherForCoordinates(
                latitude = location.latitude,
                longitude = location.longitude
            )
            weatherPreferences.saveLastCity(weather.name)
            _uiState.value = successState(weather.name, weather)
        } catch (exception: LocationPermissionException) {
            _uiState.value = WeatherUiState.Error(
                "Location permission is required to fetch current weather"
            )
        } catch (exception: LocationUnavailableException) {
            _uiState.value = WeatherUiState.Error(
                "Turn on location services and try again"
            )
        } catch (exception: IOException) {
            _uiState.value = WeatherUiState.Error(
                "Check your internet connection and try again"
            )
        } catch (exception: HttpException) {
            _uiState.value = WeatherUiState.Error(
                "Weather service returned an error (${exception.code()})"
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            _uiState.value = WeatherUiState.Error(
                "Unable to fetch weather for your location"
            )
        }
    }

    fun showError(message: String) {
        weatherJob?.cancel()
        weatherJob = null
        _uiState.value = WeatherUiState.Error(message)
    }

    private fun successState(city: String, weather: WeatherResponse): WeatherUiState.Success {
        val iconCode = weather.weather.firstOrNull()?.icon
            ?.takeIf { it.isNotBlank() }
        val iconUrl = iconCode?.let { "$WEATHER_ICON_BASE_URL/${it}@2x.png" }

        return WeatherUiState.Success(
            city = city,
            weather = weather,
            iconUrl = iconUrl
        )
    }



    private companion object {
        const val DEFAULT_CITY = "Chicago"
        const val WEATHER_ICON_BASE_URL = "https://openweathermap.org/img/wn"
    }
}

package com.prudhvi.weather.presentation.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudhvi.weather.data.CityNotFoundException
import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.LocationPermissionException
import com.prudhvi.weather.data.location.LocationProvider
import com.prudhvi.weather.data.location.LocationUnavailableException
import com.prudhvi.weather.data.remote.model.WeatherResponse
import java.io.IOException
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
        val weather: WeatherResponse
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val weatherPreferences: WeatherPreferences,
    private val locationProvider: LocationProvider
) : ViewModel() {
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

            try {
                val weather = repository.getWeatherForCity(normalizedCity)
                weatherPreferences.saveLastCity(normalizedCity)
                _uiState.value = WeatherUiState.Success(
                    city = normalizedCity,
                    weather = weather
                )
            } catch (exception: CityNotFoundException) {
                Log.w(TAG, "No geocoding result for city: $normalizedCity")
                _uiState.value = WeatherUiState.Error("We could not find that city")
            } catch (exception: IOException) {
                Log.e(TAG, "Network request failed for city: $normalizedCity", exception)
                _uiState.value = WeatherUiState.Error(
                    "Check your internet connection and try again"
                )
            } catch (exception: HttpException) {
                Log.e(
                    TAG,
                    "Weather API returned HTTP ${exception.code()} for city: $normalizedCity",
                    exception
                )
                val message = when (exception.code()) {
                    401 -> "The OpenWeather API key is invalid or not active yet"
                    429 -> "The weather service request limit was reached"
                    else -> "Weather service returned an error (${exception.code()})"
                }
                _uiState.value = WeatherUiState.Error(
                    message
                )
            } catch (exception: Exception) {
                Log.e(TAG, "Unexpected weather error for city: $normalizedCity", exception)
                _uiState.value = WeatherUiState.Error(
                    "Something went wrong. Please try again"
                )
            }
        }
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            try {
                val location = locationProvider.getCurrentLocation()
                val weather = repository.getWeatherForCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude
                )
                weatherPreferences.saveLastCity(weather.name)
                _uiState.value = WeatherUiState.Success(
                    city = weather.name,
                    weather = weather
                )
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
            } catch (exception: Exception) {
                _uiState.value = WeatherUiState.Error(
                    "Unable to fetch weather for your location"
                )
            }
        }
    }

    fun showError(message: String) {
        _uiState.value = WeatherUiState.Error(message)
    }

    private companion object {
        const val DEFAULT_CITY = "Chicago"
        const val TAG = "WeatherViewModel"
    }
}

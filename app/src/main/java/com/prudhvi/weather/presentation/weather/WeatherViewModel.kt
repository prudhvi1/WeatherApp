package com.prudhvi.weather.presentation.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prudhvi.weather.data.CityNotFoundException
import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.data.remote.model.WeatherResponse
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface WeatherUiState {
    data object Loading : WeatherUiState

    data class Success(
        val weather: WeatherResponse
    ) : WeatherUiState

    data class Error(
        val message: String
    ) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    init {
        search()
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
                _uiState.value = WeatherUiState.Success(weather)
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

    private companion object {
        const val DEFAULT_CITY = "Chicago"
        const val TAG = "WeatherViewModel"
    }
}

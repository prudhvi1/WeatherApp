package com.prudhvi.weather.shared.weather.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeocodingItemDto(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null
)

@Serializable
data class WeatherConditionDto(
    val id: Int,
    val main: String = "",
    val description: String = "",
    val icon: String = ""
)

@Serializable
data class MainWeatherDto(
    val temp: Double,
    @SerialName("feels_like") val feelsLike: Double,
    @SerialName("temp_min") val tempMin: Double = 0.0,
    @SerialName("temp_max") val tempMax: Double = 0.0,
    val pressure: Int = 0,
    val humidity: Int = 0
)

@Serializable
data class OpenWeatherResponseDto(
    val id: Int = 0,
    val name: String = "",
    val weather: List<WeatherConditionDto> = emptyList(),
    val main: MainWeatherDto
)

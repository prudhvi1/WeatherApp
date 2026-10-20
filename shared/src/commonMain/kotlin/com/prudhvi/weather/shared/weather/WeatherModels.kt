package com.prudhvi.weather.shared.weather

data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class WeatherCondition(
    val id: Int,
    val description: String,
    val icon: String
)

data class WeatherSnapshot(
    val city: String,
    val coordinates: Coordinates,
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val conditions: List<WeatherCondition>
)

sealed interface WeatherResult {
    data class Success(val snapshot: WeatherSnapshot) : WeatherResult
    data class Failure(val message: String, val retryable: Boolean = true) : WeatherResult
}

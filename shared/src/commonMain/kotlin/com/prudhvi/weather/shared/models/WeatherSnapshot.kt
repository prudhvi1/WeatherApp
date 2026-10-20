package com.prudhvi.weather.shared.models

/** A geographic point supplied by a platform location implementation. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

data class WeatherCondition(
    val id: Int,
    val description: String,
    val icon: String
)

/** Platform-neutral weather data used by UI, agents, and other clients. */
data class WeatherSnapshot(
    val city: String,
    val coordinates: Coordinates,
    val temperature: Double,
    val feelsLike: Double,
    val humidityPercent: Int,
    val conditions: List<WeatherCondition>
)

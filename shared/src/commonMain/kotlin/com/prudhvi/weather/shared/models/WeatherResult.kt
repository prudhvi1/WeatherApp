package com.prudhvi.weather.shared.models

/** Result returned by shared weather operations. */
sealed interface WeatherResult {
    data class Success(val snapshot: WeatherSnapshot) : WeatherResult
    data class Failure(
        val message: String,
        val retryable: Boolean = true
    ) : WeatherResult
}

package com.prudhvi.weather.shared.weather

import com.prudhvi.weather.shared.models.Coordinates
import com.prudhvi.weather.shared.models.WeatherResult

/** Platform adapter used when the use case is responsible for resolving location. */
fun interface CurrentLocationProvider {
    suspend fun currentLocation(): Coordinates?
}

/** Retrieves weather after a platform has resolved the device's current coordinates. */
class GetWeatherForCurrentLocation(
    private val repository: WeatherRepository,
    private val locationProvider: CurrentLocationProvider? = null
) {
    suspend operator fun invoke(coordinates: Coordinates): WeatherResult =
        repository.getWeatherForCoordinates(
            latitude = coordinates.latitude,
            longitude = coordinates.longitude
        )

    suspend operator fun invoke(): WeatherResult {
        val coordinates = locationProvider?.currentLocation()
            ?: return WeatherResult.Failure(
                message = "Current location is unavailable",
                retryable = false
            )
        return invoke(coordinates)
    }
}

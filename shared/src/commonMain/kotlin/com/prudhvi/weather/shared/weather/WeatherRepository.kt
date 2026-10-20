package com.prudhvi.weather.shared.weather

import com.prudhvi.weather.shared.models.Coordinates
import com.prudhvi.weather.shared.models.WeatherCondition
import com.prudhvi.weather.shared.models.WeatherResult
import com.prudhvi.weather.shared.models.WeatherSnapshot
import com.prudhvi.weather.shared.weather.remote.OpenWeatherApiClient

/** Source of weather data for the shared domain layer. */
interface WeatherRepository {
    suspend fun getWeatherForCity(city: String): WeatherResult
    suspend fun getWeatherForCoordinates(latitude: Double, longitude: Double): WeatherResult
}

class KtorWeatherRepository private constructor(
    private val apiClient: OpenWeatherApiClient
) : WeatherRepository {

    override suspend fun getWeatherForCity(city: String): WeatherResult {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) {
            return WeatherResult.Failure("City name cannot be empty")
        }

        return runCatching {
            val location = apiClient.geocode(trimmed).firstOrNull()
                ?: return WeatherResult.Failure("We could not find location '$trimmed'")

            getWeatherForCoordinates(location.lat, location.lon, locationName = location.name)
        }.getOrElse { error ->
            WeatherResult.Failure(error.message ?: "Failed to fetch weather for $trimmed")
        }
    }

    override suspend fun getWeatherForCoordinates(
        latitude: Double,
        longitude: Double
    ): WeatherResult = getWeatherForCoordinates(latitude, longitude, locationName = null)

    private suspend fun getWeatherForCoordinates(
        latitude: Double,
        longitude: Double,
        locationName: String?
    ): WeatherResult {
        return runCatching {
            val dto = apiClient.currentWeather(latitude, longitude)

            val snapshot = WeatherSnapshot(
                city = locationName ?: dto.name.ifEmpty { "Current Location" },
                coordinates = Coordinates(latitude, longitude),
                temperature = dto.main.temp,
                feelsLike = dto.main.feelsLike,
                humidityPercent = dto.main.humidity,
                conditions = dto.weather.map {
                    WeatherCondition(
                        id = it.id,
                        description = it.description,
                        icon = it.icon
                    )
                }
            )
            WeatherResult.Success(snapshot)
        }.getOrElse { error ->
            WeatherResult.Failure(error.message ?: "Failed to fetch weather for coordinates")
        }
    }

    companion object {
        fun create(apiKey: String): KtorWeatherRepository = KtorWeatherRepository(
            apiClient = OpenWeatherApiClient.create(apiKey)
        )
    }
}

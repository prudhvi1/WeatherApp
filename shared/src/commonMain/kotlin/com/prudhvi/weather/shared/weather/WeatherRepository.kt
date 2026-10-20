package com.prudhvi.weather.shared.weather

import com.prudhvi.weather.shared.weather.remote.GeocodingItemDto
import com.prudhvi.weather.shared.weather.remote.OpenWeatherResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface WeatherRepository {
    suspend fun getWeatherForCity(city: String): WeatherResult
    suspend fun getWeatherForCoordinates(latitude: Double, longitude: Double): WeatherResult
}

class KtorWeatherRepository(
    private val apiKey: String,
    private val httpClient: HttpClient = createDefaultHttpClient()
) : WeatherRepository {

    override suspend fun getWeatherForCity(city: String): WeatherResult {
        val trimmed = city.trim()
        if (trimmed.isEmpty()) {
            return WeatherResult.Failure("City name cannot be empty")
        }

        return runCatching {
            val geocodingResults: List<GeocodingItemDto> = httpClient.get(GEOCODING_URL) {
                parameter("q", trimmed)
                parameter("limit", 1)
                parameter("appid", apiKey)
            }.body()

            val location = geocodingResults.firstOrNull()
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
            val dto: OpenWeatherResponseDto = httpClient.get(WEATHER_URL) {
                parameter("lat", latitude)
                parameter("lon", longitude)
                parameter("units", "imperial")
                parameter("appid", apiKey)
            }.body()

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
        private const val BASE_URL = "https://api.openweathermap.org"
        private const val GEOCODING_URL = "$BASE_URL/geo/1.0/direct"
        private const val WEATHER_URL = "$BASE_URL/data/2.5/weather"

        fun createDefaultHttpClient(): HttpClient = HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }
}

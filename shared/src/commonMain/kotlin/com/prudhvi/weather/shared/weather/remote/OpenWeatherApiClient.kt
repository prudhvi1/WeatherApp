package com.prudhvi.weather.shared.weather.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** but Ktor transport client for the OpenWeather HTTP API. */
class OpenWeatherApiClient private constructor(
    private val apiKey: String,
    private val httpClient: HttpClient
) {
    suspend fun geocode(city: String): List<GeocodingItemDto> =
        httpClient.get(GEOCODING_URL) {
            parameter("q", city)
            parameter("limit", 1)
            parameter("appid", apiKey)
        }.body()

    suspend fun currentWeather(
        latitude: Double,
        longitude: Double
    ): OpenWeatherResponseDto = httpClient.get(WEATHER_URL) {
        parameter("lat", latitude)
        parameter("lon", longitude)
        parameter("units", "imperial")
        parameter("appid", apiKey)
    }.body()

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org"
        private const val GEOCODING_URL = "$BASE_URL/geo/1.0/direct"
        private const val WEATHER_URL = "$BASE_URL/data/2.5/weather"

        fun create(apiKey: String): OpenWeatherApiClient = OpenWeatherApiClient(
            apiKey = apiKey,
            httpClient = HttpClient {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    })
                }
            }
        )
    }
}

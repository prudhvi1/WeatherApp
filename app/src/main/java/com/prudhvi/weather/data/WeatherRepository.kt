package com.prudhvi.weather.data

import android.util.Log
import com.prudhvi.weather.data.remote.GeocodingApiService
import com.prudhvi.weather.data.remote.WeatherApiService
import com.prudhvi.weather.data.remote.model.WeatherResponse

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val geocodingApiService: GeocodingApiService,
    private val apiKey: String
) {
    suspend fun getWeatherForCity(city: String): WeatherResponse {
        val normalizedCity = city.trim()
        require(normalizedCity.isNotEmpty()) { "City name cannot be empty" }

        Log.d(TAG, "Weather lookup requested for city: $normalizedCity")

        val location = geocodingApiService
            .findLocations(query = normalizedCity, apiKey = apiKey)
            .firstOrNull()
            ?: throw CityNotFoundException(normalizedCity)

        return getWeatherForCoordinates(location.lat, location.lon)
    }

    suspend fun getWeatherForCoordinates(
        latitude: Double,
        longitude: Double
    ): WeatherResponse = weatherApiService.getCurrentWeather(
        latitude = latitude,
        longitude = longitude,
        apiKey = apiKey
    )
}

class CityNotFoundException(city: String) : Exception("No weather location found for $city")

private const val TAG = "WeatherRepository"

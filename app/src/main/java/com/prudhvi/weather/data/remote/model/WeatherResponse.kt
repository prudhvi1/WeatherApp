package com.prudhvi.weather.data.remote.model

/**
 * OpenWeather's current weather response.
 *
 * The API returns temperatures in Kelvin by default. The Retrofit service
 * requests imperial units, so these values are Fahrenheit for this app.
 */
data class WeatherResponse(
    val coord: Coordinates,
    val weather: List<WeatherCondition>,
    val main: MainWeather,
    val wind: Wind,
    val sys: WeatherSystem,
    val name: String
)

data class Coordinates(
    val lon: Double,
    val lat: Double
)

data class WeatherCondition(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class MainWeather(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class Wind(
    val speed: Double,
    val deg: Int?
)

data class WeatherSystem(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

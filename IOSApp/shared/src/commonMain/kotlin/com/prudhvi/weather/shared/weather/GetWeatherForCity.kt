package com.prudhvi.weather.shared.weather

import com.prudhvi.weather.shared.models.WeatherResult

/** Retrieves weather for a user-entered city name. */
class GetWeatherForCity(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(city: String): WeatherResult =
        repository.getWeatherForCity(city)
}

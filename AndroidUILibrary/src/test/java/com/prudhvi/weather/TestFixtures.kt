package com.prudhvi.weather

import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.CurrentLocationProvider
import com.prudhvi.weather.data.location.UserLocation
import com.prudhvi.weather.shared.models.Coordinates
import com.prudhvi.weather.shared.models.WeatherCondition
import com.prudhvi.weather.shared.models.WeatherResult
import com.prudhvi.weather.shared.models.WeatherSnapshot
import com.prudhvi.weather.shared.weather.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeWeatherRepository(
    private val locations: Set<String> = setOf("Chicago", "New York"),
    private val failure: Throwable? = null
) : WeatherRepository {
    var lastQuery: String? = null

    override suspend fun getWeatherForCity(city: String): WeatherResult {
        lastQuery = city
        failure?.let { throw it }
        return if (city in locations) {
            WeatherResult.Success(snapshot(city))
        } else {
            WeatherResult.Failure("We could not find location '$city'")
        }
    }

    override suspend fun getWeatherForCoordinates(
        latitude: Double,
        longitude: Double
    ): WeatherResult {
        failure?.let { throw it }
        return WeatherResult.Success(
            snapshot("Chicago").copy(coordinates = Coordinates(latitude, longitude))
        )
    }

    private fun snapshot(city: String) = WeatherSnapshot(
        city = city,
        coordinates = Coordinates(41.88, -87.63),
        temperature = 72.0,
        feelsLike = 70.0,
        humidityPercent = 45,
        conditions = listOf(WeatherCondition(800, "clear sky", "01d"))
    )
}

internal class FakeWeatherPreferences(initialCity: String? = null) : WeatherPreferences {
    private val savedCity = MutableStateFlow(initialCity)
    override val lastCity: Flow<String?> = savedCity

    override suspend fun saveLastCity(city: String) {
        savedCity.value = city
    }

    fun savedCity(): String? = savedCity.value
}

internal class FakeCurrentLocationProvider(
    private val location: UserLocation = UserLocation(41.88, -87.63),
    private val failure: Throwable? = null
) : CurrentLocationProvider {
    override suspend fun getCurrentLocation(): UserLocation {
        failure?.let { throw it }
        return location
    }
}

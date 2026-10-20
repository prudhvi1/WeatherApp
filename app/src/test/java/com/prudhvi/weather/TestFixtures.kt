package com.prudhvi.weather

import com.prudhvi.weather.data.remote.GeocodingApiService
import com.prudhvi.weather.data.remote.WeatherApiService
import com.prudhvi.weather.data.remote.model.Coordinates
import com.prudhvi.weather.data.remote.model.GeocodingLocation
import com.prudhvi.weather.data.remote.model.MainWeather
import com.prudhvi.weather.data.remote.model.WeatherCondition
import com.prudhvi.weather.data.remote.model.WeatherResponse
import com.prudhvi.weather.data.remote.model.WeatherSystem
import com.prudhvi.weather.data.remote.model.Wind
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.CurrentLocationProvider
import com.prudhvi.weather.data.location.UserLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal fun weatherResponse(name: String = "Chicago") = WeatherResponse(
    coord = Coordinates(lon = -87.63, lat = 41.88),
    weather = listOf(WeatherCondition(800, "Clear", "clear sky", "01d")),
    main = MainWeather(72.0, 70.0, 68.0, 75.0, 1012, 45),
    wind = Wind(4.1, 80),
    sys = WeatherSystem("US", 1_000L, 2_000L),
    name = name
)

internal class FakeWeatherApiService(
    private val response: WeatherResponse = weatherResponse(),
    private val failure: Throwable? = null
) : WeatherApiService {
    var lastLatitude: Double? = null
    var lastLongitude: Double? = null

    override suspend fun getCurrentWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        units: String
    ): WeatherResponse {
        lastLatitude = latitude
        lastLongitude = longitude
        failure?.let { throw it }
        return response
    }
}

internal class FakeGeocodingApiService(
    private val locations: List<GeocodingLocation> = listOf(
        GeocodingLocation("Chicago", 41.88, -87.63, "US", "Illinois")
    ),
    private val failure: Throwable? = null
) : GeocodingApiService {
    var lastQuery: String? = null

    override suspend fun findLocations(
        query: String,
        limit: Int,
        apiKey: String
    ): List<GeocodingLocation> {
        lastQuery = query
        failure?.let { throw it }
        return locations
    }
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

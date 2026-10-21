package com.prudhvi.weather.shared

import com.prudhvi.weather.shared.models.Coordinates
import com.prudhvi.weather.shared.models.WeatherCondition
import com.prudhvi.weather.shared.models.WeatherResult
import com.prudhvi.weather.shared.models.WeatherSnapshot
import com.prudhvi.weather.shared.weather.CurrentLocationProvider
import com.prudhvi.weather.shared.weather.GetClothingRecommendation
import com.prudhvi.weather.shared.weather.GetWeatherForCity
import com.prudhvi.weather.shared.weather.GetWeatherForCurrentLocation
import com.prudhvi.weather.shared.weather.WeatherRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WeatherUseCasesTest {
    private val snapshot = WeatherSnapshot(
        city = "Chicago",
        coordinates = Coordinates(41.88, -87.63),
        temperature = 42.0,
        feelsLike = 38.0,
        humidityPercent = 65,
        conditions = listOf(WeatherCondition(500, "light rain", "10d"))
    )

    private val repository = object : WeatherRepository {
        override suspend fun getWeatherForCity(city: String): WeatherResult =
            WeatherResult.Success(snapshot.copy(city = city))

        override suspend fun getWeatherForCoordinates(
            latitude: Double,
            longitude: Double
        ): WeatherResult = WeatherResult.Success(snapshot.copy(coordinates = Coordinates(latitude, longitude)))
    }

    @Test
    fun cityUseCaseDelegatesToRepository() = runTest {
        val result = GetWeatherForCity(repository)("Chicago")

        assertEquals("Chicago", assertIs<WeatherResult.Success>(result).snapshot.city)
    }

    @Test
    fun currentLocationUseCaseResolvesCoordinates() = runTest {
        val provider = CurrentLocationProvider { Coordinates(40.0, -86.0) }
        val result = GetWeatherForCurrentLocation(repository, provider)()

        assertEquals(
            Coordinates(40.0, -86.0),
            assertIs<WeatherResult.Success>(result).snapshot.coordinates
        )
    }

    @Test
    fun clothingRecommendationUsesSharedWeatherRules() {
        assertEquals(
            "Bring an umbrella and a waterproof jacket!",
            GetClothingRecommendation()(snapshot)
        )
    }
}

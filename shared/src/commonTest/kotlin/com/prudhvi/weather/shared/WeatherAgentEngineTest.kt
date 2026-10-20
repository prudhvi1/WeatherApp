package com.prudhvi.weather.shared

import com.prudhvi.weather.shared.agent.AgentAction
import com.prudhvi.weather.shared.agent.AgentRequest
import com.prudhvi.weather.shared.agent.DeviceCapabilities
import com.prudhvi.weather.shared.agent.WeatherAgentEngine
import com.prudhvi.weather.shared.weather.Coordinates
import com.prudhvi.weather.shared.weather.WeatherCondition
import com.prudhvi.weather.shared.weather.WeatherRepository
import com.prudhvi.weather.shared.weather.WeatherResult
import com.prudhvi.weather.shared.weather.WeatherSnapshot
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeatherAgentEngineTest {

    private val fakeSnapshot = WeatherSnapshot(
        city = "Chicago",
        coordinates = Coordinates(41.8781, -87.6298),
        temperature = 42.0,
        feelsLike = 38.0,
        humidityPercent = 65,
        conditions = listOf(WeatherCondition(500, "light rain", "10d"))
    )

    private val fakeRepository = object : WeatherRepository {
        override suspend fun getWeatherForCity(city: String): WeatherResult {
            return WeatherResult.Success(fakeSnapshot.copy(city = city))
        }

        override suspend fun getWeatherForCoordinates(
            latitude: Double,
            longitude: Double
        ): WeatherResult {
            return WeatherResult.Success(fakeSnapshot)
        }
    }

    private class FakeDeviceCapabilities : DeviceCapabilities {
        var spokenText: String? = null
        override suspend fun currentLocation(): Coordinates = Coordinates(41.8781, -87.6298)
        override suspend fun speak(text: String) {
            spokenText = text
        }
        override suspend fun openAppSettings() {}
    }

    @Test
    fun agentRespondsWithWeatherAndClothingAdviceForCity() = runTest {
        val fakeDevice = FakeDeviceCapabilities()
        val agent = WeatherAgentEngine(fakeRepository, fakeDevice)

        val response = agent.respond(AgentRequest("What's the weather in Chicago?"))

        assertEquals(AgentAction.SearchWeather("Chicago"), response.action)
        assertTrue(response.text.contains("Chicago"))
        assertTrue(response.text.contains("42°F"))
        assertTrue(response.text.contains("light rain"))
        assertTrue(response.text.contains("umbrella"))
        assertEquals(response.text, fakeDevice.spokenText)
    }

    @Test
    fun agentRespondsForCurrentLocation() = runTest {
        val fakeDevice = FakeDeviceCapabilities()
        val agent = WeatherAgentEngine(fakeRepository, fakeDevice)

        val response = agent.respond(AgentRequest("Check weather at my location"))

        assertEquals(AgentAction.FetchCurrentLocationWeather, response.action)
        assertTrue(response.text.contains("Chicago"))
        assertEquals(response.text, fakeDevice.spokenText)
    }
}

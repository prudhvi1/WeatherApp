package com.prudhvi.weather.shared

import com.prudhvi.weather.shared.agent.AgentAction
import com.prudhvi.weather.shared.agent.AgentResponse
import com.prudhvi.weather.shared.config.WhiteLabelConfig
import com.prudhvi.weather.shared.weather.Coordinates
import com.prudhvi.weather.shared.weather.WeatherCondition
import com.prudhvi.weather.shared.weather.WeatherSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedModelsTest {
    @Test
    fun whiteLabelConfigAndWeatherSnapshotPreserveHostValues() {
        val config = WhiteLabelConfig("Demo Weather", 0xFF123456, 0xFF654321, "https://example.com/icons/")
        val snapshot = WeatherSnapshot(
            city = "Bloomington",
            coordinates = Coordinates(39.16, -86.53),
            temperature = 72.0,
            feelsLike = 71.0,
            humidityPercent = 45,
            conditions = listOf(WeatherCondition(800, "clear sky", "01d"))
        )

        assertEquals("Demo Weather", config.appName)
        assertEquals("Bloomington", snapshot.city)
        assertEquals(AgentAction.SearchWeather("Bloomington"), AgentResponse("", AgentAction.SearchWeather("Bloomington")).action)
    }
}

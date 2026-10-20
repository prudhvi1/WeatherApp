package com.prudhvi.weather

import com.google.gson.Gson
import com.prudhvi.weather.data.remote.model.WeatherResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherResponseMappingTest {
    @Test
    fun mapsOpenWeatherJsonToWeatherResponse() {
        val json = """
            {
              "coord": { "lon": -87.63, "lat": 41.88 },
              "weather": [{ "id": 800, "main": "Clear", "description": "clear sky", "icon": "01d" }],
              "main": { "temp": 72.0, "feels_like": 70.0, "temp_min": 68.0, "temp_max": 75.0, "pressure": 1012, "humidity": 45 },
              "wind": { "speed": 4.1, "deg": 80 },
              "sys": { "country": "US", "sunrise": 1000, "sunset": 2000 },
              "name": "Chicago"
            }
        """.trimIndent()

        val response = Gson().fromJson(json, WeatherResponse::class.java)

        assertEquals("Chicago", response.name)
        assertEquals(72.0, response.main.temp, 0.0)
        assertEquals("clear sky", response.weather.single().description)
        assertEquals("01d", response.weather.single().icon)
    }
}

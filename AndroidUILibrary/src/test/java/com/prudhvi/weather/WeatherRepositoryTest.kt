package com.prudhvi.weather

import com.prudhvi.weather.data.CityNotFoundException
import com.prudhvi.weather.data.WeatherRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherRepositoryTest {
    @Test
    fun citySearchGeocodesThenRequestsWeatherCoordinates() = kotlinx.coroutines.test.runTest {
        val geocoding = FakeGeocodingApiService()
        val weather = FakeWeatherApiService()
        val repository = WeatherRepository(weather, geocoding, "test-key")

        val result = repository.getWeatherForCity("  Chicago ")

        assertEquals("Chicago", geocoding.lastQuery)
        assertEquals(41.88, weather.lastLatitude!!, 0.0)
        assertEquals(-87.63, weather.lastLongitude!!, 0.0)
        assertEquals("Chicago", result.name)
    }

    @Test
    fun citySearchThrowsWhenGeocodingReturnsNoLocations() = kotlinx.coroutines.test.runTest {
        val repository = WeatherRepository(
            FakeWeatherApiService(),
            FakeGeocodingApiService(locations = emptyList()),
            "test-key"
        )

        val exception = try {
            repository.getWeatherForCity("Unknown")
            null
        } catch (error: Throwable) {
            error
        }

        assertTrue(exception is CityNotFoundException)
    }
}

package com.prudhvi.weather

import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.presentation.weather.WeatherUiState
import com.prudhvi.weather.presentation.weather.WeatherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsLoadingThenEmitsSuccessAndPersistsCity() = runTest {
        val preferences = FakeWeatherPreferences()
        val viewModel = createViewModel(preferences)

        assertTrue(viewModel.uiState.value is WeatherUiState.Loading)
        advanceUntilIdle()

        viewModel.search("New York")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("New York", (state as WeatherUiState.Success).city)
        assertEquals("New York", preferences.savedCity())
    }

    @Test
    fun emitsErrorForUnknownCity() = runTest {
        val preferences = FakeWeatherPreferences()
        val viewModel = createViewModel(preferences, locations = emptyList())

        advanceUntilIdle()

        assertEquals(
            WeatherUiState.Error("We could not find that city"),
            viewModel.uiState.value
        )
    }

    @Test
    fun loadsPersistedCityOnStartup() = runTest {
        val preferences = FakeWeatherPreferences("Bloomington")
        val geocoding = FakeGeocodingApiService(
            locations = listOf(
                com.prudhvi.weather.data.remote.model.GeocodingLocation(
                    "Bloomington", 40.48, -88.99, "US", "Illinois"
                )
            )
        )
        val viewModel = WeatherViewModel(
            repository = WeatherRepository(
                FakeWeatherApiService(weatherResponse("Bloomington")),
                geocoding,
                "test-key"
            ),
            weatherPreferences = preferences,
            locationProvider = FakeCurrentLocationProvider()
        )

        advanceUntilIdle()

        assertEquals("Bloomington", geocoding.lastQuery)
        assertTrue(viewModel.uiState.value is WeatherUiState.Success)
    }

    @Test
    fun fetchesCurrentLocationSuccessfully() = runTest {
        val preferences = FakeWeatherPreferences()
        val viewModel = createViewModel(preferences)

        advanceUntilIdle()

        viewModel.fetchCurrentLocation()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals("Chicago", (state as WeatherUiState.Success).city)
    }

    @Test
    fun handlesLocationPermissionError() = runTest {
        val preferences = FakeWeatherPreferences()
        val viewModel = WeatherViewModel(
            repository = WeatherRepository(
                FakeWeatherApiService(),
                FakeGeocodingApiService(),
                "test-key"
            ),
            weatherPreferences = preferences,
            locationProvider = FakeCurrentLocationProvider(
                failure = com.prudhvi.weather.data.location.LocationPermissionException()
            )
        )

        advanceUntilIdle()

        viewModel.fetchCurrentLocation()
        advanceUntilIdle()

        assertEquals(
            WeatherUiState.Error("Location permission is required to fetch current weather"),
            viewModel.uiState.value
        )
    }

    private fun createViewModel(
        preferences: FakeWeatherPreferences,
        locations: List<com.prudhvi.weather.data.remote.model.GeocodingLocation> = listOf(
            com.prudhvi.weather.data.remote.model.GeocodingLocation(
                "Chicago", 41.88, -87.63, "US", "Illinois"
            )
        )
    ): WeatherViewModel = WeatherViewModel(
        repository = WeatherRepository(
            FakeWeatherApiService(),
            FakeGeocodingApiService(locations = locations),
            "test-key"
        ),
        weatherPreferences = preferences,
        locationProvider = FakeCurrentLocationProvider()
    )
}

package com.prudhvi.weather.di

import android.content.Context
import com.prudhvi.weather.data.local.DataStoreWeatherPreferences
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.CurrentLocationProvider
import com.prudhvi.weather.data.location.LocationProvider
import com.prudhvi.weather.shared.weather.KtorWeatherRepository
import com.prudhvi.weather.shared.weather.WeatherRepository

/**
 * Application-scoped dependencies used by the weather feature.
 *
 * Keeping construction here lets consumers receive shared interfaces through
 * constructors instead of creating network clients inside screens or ViewModels.
 */
class AppContainer(
    context: Context,
    val apiKey: String
) {
    val locationProvider: CurrentLocationProvider by lazy {
        LocationProvider(context)
    }

    val weatherPreferences: WeatherPreferences by lazy {
        DataStoreWeatherPreferences(context)
    }

    val weatherRepository: WeatherRepository by lazy {
        KtorWeatherRepository.create(apiKey = apiKey)
    }
}

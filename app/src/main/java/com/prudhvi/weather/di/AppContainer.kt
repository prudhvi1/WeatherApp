package com.prudhvi.weather.di

import android.content.Context
import com.prudhvi.weather.data.WeatherRepository
import com.prudhvi.weather.data.local.WeatherPreferences
import com.prudhvi.weather.data.location.LocationProvider
import com.prudhvi.weather.data.remote.GeocodingApiService
import com.prudhvi.weather.data.remote.WeatherApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Application-scoped dependencies used by the weather feature.
 *
 * Keeping construction here lets consumers receive interfaces through
 * constructors instead of creating Retrofit clients inside screens or
 * ViewModels.
 */
class AppContainer(
    context: Context,
    val apiKey: String,
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
) {
    val locationProvider: LocationProvider by lazy {
        LocationProvider(context)
    }

    val weatherPreferences: WeatherPreferences by lazy {
        WeatherPreferences(context)
    }

    val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }

    val geocodingApiService: GeocodingApiService by lazy {
        retrofit.create(GeocodingApiService::class.java)
    }

    val weatherRepository: WeatherRepository by lazy {
        WeatherRepository(
            weatherApiService = weatherApiService,
            geocodingApiService = geocodingApiService,
            apiKey = apiKey
        )
    }

    private companion object {
        const val BASE_URL = "https://api.openweathermap.org/"
    }
}

package com.prudhvi.weather.data.remote

import com.prudhvi.weather.data.remote.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = IMPERIAL_UNITS
    ): WeatherResponse

    private companion object {
        const val IMPERIAL_UNITS = "imperial"
    }
}

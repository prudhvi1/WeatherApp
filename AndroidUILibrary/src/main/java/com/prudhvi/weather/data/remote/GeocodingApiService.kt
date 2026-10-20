package com.prudhvi.weather.data.remote

import com.prudhvi.weather.data.remote.model.GeocodingLocation
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {
    @GET("geo/1.0/direct")
    suspend fun findLocations(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodingLocation>
}

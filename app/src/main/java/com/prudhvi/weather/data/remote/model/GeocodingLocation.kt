package com.prudhvi.weather.data.remote.model

data class GeocodingLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String?
)

package com.prudhvi.weather.shared.config

/**
 * Product branding and feature switches supplied by the host application.
 * Keeping this data outside the weather domain lets the same shared module
 * power multiple branded Android and iOS applications.
 */
data class WhiteLabelConfig(
    val appName: String,
    val brandPrimaryColor: Long,
    val brandSecondaryColor: Long,
    val weatherIconBaseUrl: String,
    val assistantEnabled: Boolean = true,
    val locationWeatherEnabled: Boolean = true
)

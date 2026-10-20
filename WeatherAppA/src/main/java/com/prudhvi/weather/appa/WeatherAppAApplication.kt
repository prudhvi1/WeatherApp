package com.prudhvi.weather.appa

import com.prudhvi.weather.BaseWeatherApplication
import com.prudhvi.weather.shared.config.WhiteLabelConfig

class WeatherAppAApplication : BaseWeatherApplication() {
    override val whiteLabelConfig = WhiteLabelConfig(
        appName = "Weather App A",
        brandPrimaryColor = 0xFF1976D2,
        brandSecondaryColor = 0xFF42A5F5,
        weatherIconBaseUrl = "https://openweathermap.org/img/wn/",
        assistantEnabled = true,
        locationWeatherEnabled = true
    )

    override val openWeatherApiKey: String
        get() = BuildConfig.OPENWEATHER_API_KEY
}

package com.prudhvi.weather

import android.app.Application
import com.prudhvi.weather.di.AppContainer
import com.prudhvi.weather.shared.config.WhiteLabelConfig

/** Shared application lifecycle and dependency setup for white-label variants. */
open class BaseWeatherApplication : Application() {
    open val whiteLabelConfig: WhiteLabelConfig = WhiteLabelConfig(
        appName = "Weather",
        brandPrimaryColor = 0xFF1976D2,
        brandSecondaryColor = 0xFF42A5F5,
        weatherIconBaseUrl = "https://openweathermap.org/img/wn/",
        assistantEnabled = true,
        locationWeatherEnabled = true
    )

    protected open val openWeatherApiKey: String
        get() = BuildConfig.OPENWEATHER_API_KEY

    val appContainer: AppContainer by lazy {
        AppContainer(context = this, apiKey = openWeatherApiKey)
    }
}

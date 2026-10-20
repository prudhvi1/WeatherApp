package com.prudhvi.weather

import android.app.Application
import com.prudhvi.weather.di.AppContainer

class WeatherApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(
            context = this,
            apiKey = BuildConfig.OPENWEATHER_API_KEY
        )
    }
}

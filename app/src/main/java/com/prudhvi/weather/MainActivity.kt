package com.prudhvi.weather

import android.util.Log

class MainActivity : BaseWeatherActivity() {

    override fun onResume() {
        super.onResume()
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "OpenWeather API key configured: " +
                    "${BuildConfig.OPENWEATHER_API_KEY.isNotBlank()}, " +
                    "length: ${BuildConfig.OPENWEATHER_API_KEY.length}"
            )
        }

    }

    private companion object {
        const val TAG = "WeatherConfig"
    }
}

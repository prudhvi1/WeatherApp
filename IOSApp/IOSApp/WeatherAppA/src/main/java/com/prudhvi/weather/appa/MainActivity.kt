package com.prudhvi.weather.appa

import android.util.Log
import com.prudhvi.weather.BaseWeatherActivity

class MainActivity : BaseWeatherActivity() {

    override fun onResume() {
        super.onResume()
        val app = application as WeatherAppAApplication
        Log.d("WeatherAppA", "Launching ${app.whiteLabelConfig.appName}")
    }
}

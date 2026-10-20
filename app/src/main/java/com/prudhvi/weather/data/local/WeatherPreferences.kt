package com.prudhvi.weather.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.weatherDataStore by preferencesDataStore(name = "weather_preferences")

class WeatherPreferences(
    private val context: Context
) {
    val lastCity: Flow<String?> = context.weatherDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences: Preferences ->
            preferences[LAST_CITY]
        }

    suspend fun saveLastCity(city: String) {
        context.weatherDataStore.edit { preferences ->
            preferences[LAST_CITY] = city
        }
    }

    private companion object {
        val LAST_CITY = stringPreferencesKey("last_city")
    }
}

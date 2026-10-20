package com.prudhvi.weather.shared.weather

import com.prudhvi.weather.shared.models.WeatherSnapshot

/** Turns weather conditions into a concise, platform-neutral clothing suggestion. */
class GetClothingRecommendation {
    operator fun invoke(snapshot: WeatherSnapshot): String {
        val temperatureF = snapshot.temperature.toInt()
        val condition = snapshot.conditions
            .firstOrNull()
            ?.description
            .orEmpty()
            .lowercase()
        val isRainy = condition.contains("rain") || condition.contains("drizzle")

        return when {
            isRainy && temperatureF < 55 -> "Bring an umbrella and a waterproof jacket!"
            isRainy -> "Bring an umbrella!"
            temperatureF < 55 -> "You should wear a warm coat."
            temperatureF > 80 -> "It's warm, dress lightly!"
            else -> "Comfortable weather for outdoors!"
        }
    }
}

package com.prudhvi.weather.shared.agent

import com.prudhvi.weather.shared.models.WeatherResult
import com.prudhvi.weather.shared.models.WeatherSnapshot
import com.prudhvi.weather.shared.weather.GetClothingRecommendation
import com.prudhvi.weather.shared.weather.GetWeatherForCity
import com.prudhvi.weather.shared.weather.GetWeatherForCurrentLocation
import com.prudhvi.weather.shared.weather.WeatherRepository

/**
 * Shared natural-language weather behavior. Platform code supplies the repository
 * and optional device capabilities; all weather decisions stay in commonMain.
 */
class WeatherAgentEngine(
    private val getWeatherForCity: GetWeatherForCity,
    private val getWeatherForCurrentLocation: GetWeatherForCurrentLocation,
    private val getClothingRecommendation: GetClothingRecommendation = GetClothingRecommendation(),
    private val deviceCapabilities: DeviceCapabilities? = null
) : WeatherAgent {

    /** Convenience constructor retained for clients that provide a repository directly. */
    constructor(
        weatherRepository: WeatherRepository,
        deviceCapabilities: DeviceCapabilities? = null
    ) : this(
        getWeatherForCity = GetWeatherForCity(weatherRepository),
        getWeatherForCurrentLocation = GetWeatherForCurrentLocation(weatherRepository),
        getClothingRecommendation = GetClothingRecommendation(),
        deviceCapabilities = deviceCapabilities
    )

    override suspend fun respond(request: AgentRequest): AgentResponse {
        val input = request.text.trim()
        if (input.isEmpty()) {
            return AgentResponse(
                text = "Hello! Ask me about the weather anywhere, or try 'weather at my location'."
            )
        }

        val lowerInput = input.lowercase()

        if (lowerInput.contains("my location") || lowerInput.contains("here") || lowerInput.contains("current location")) {
            val coords = deviceCapabilities?.currentLocation()
            if (coords != null) {
                return when (val result = getWeatherForCurrentLocation(coords)) {
                    is WeatherResult.Success -> {
                        val speechText = formatWeatherAdvice(result.snapshot)
                        speakIfSupported(speechText)
                        AgentResponse(text = speechText, action = AgentAction.FetchCurrentLocationWeather)
                    }
                    is WeatherResult.Failure -> {
                        AgentResponse(text = "I couldn't fetch weather for your location: ${result.message}")
                    }
                }
            } else {
                return AgentResponse(
                    text = "Checking weather for your current location...",
                    action = AgentAction.FetchCurrentLocationWeather
                )
            }
        }

        val targetCity = extractCityName(input)
        if (targetCity != null) {
            return when (val result = getWeatherForCity(targetCity)) {
                is WeatherResult.Success -> {
                    val speechText = formatWeatherAdvice(result.snapshot)
                    speakIfSupported(speechText)
                    AgentResponse(text = speechText, action = AgentAction.SearchWeather(targetCity))
                }
                is WeatherResult.Failure -> {
                    AgentResponse(text = "Sorry, I couldn't find weather data for '$targetCity'.")
                }
            }
        }

        val defaultText = "I can help with weather updates and clothing recommendations! Try asking 'Weather in Chicago' or 'Do I need a jacket in Tokyo?'"
        return AgentResponse(text = defaultText)
    }

    private suspend fun speakIfSupported(text: String) {
        runCatching {
            deviceCapabilities?.speak(text)
        }
    }

    private fun extractCityName(input: String): String? {
        val cleaned = input
            .replace(Regex("(?i)what's the weather in"), "")
            .replace(Regex("(?i)weather in"), "")
            .replace(Regex("(?i)how is the weather in"), "")
            .replace(Regex("(?i)check weather for"), "")
            .replace(Regex("(?i)weather for"), "")
            .replace(Regex("(?i)should i wear a jacket in"), "")
            .replace(Regex("(?i)is it raining in"), "")
            .replace(Regex("[?!.]"), "")
            .trim()

        return if (cleaned.isNotEmpty() && cleaned.length > 1) cleaned else null
    }

    private fun formatWeatherAdvice(snapshot: WeatherSnapshot): String {
        val tempF = snapshot.temperature.toInt()
        val conditionDesc = snapshot.conditions.firstOrNull()?.description ?: "clear skies"
        val clothingAdvice = getClothingRecommendation(snapshot)
        return "In ${snapshot.city}, it's currently $tempF°F with $conditionDesc. $clothingAdvice"
    }
}

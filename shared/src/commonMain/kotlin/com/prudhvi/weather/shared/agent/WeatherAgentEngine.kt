package com.prudhvi.weather.shared.agent

import com.prudhvi.weather.shared.weather.WeatherRepository
import com.prudhvi.weather.shared.weather.WeatherResult
import com.prudhvi.weather.shared.weather.WeatherSnapshot

class WeatherAgentEngine(
    private val weatherRepository: WeatherRepository,
    private val deviceCapabilities: DeviceCapabilities? = null
) : WeatherAgent {

    override suspend fun respond(request: AgentRequest): AgentResponse {
        val input = request.text.trim()
        if (input.isEmpty()) {
            return AgentResponse(
                text = "Hello! Ask me about the weather anywhere, or try 'weather at my location'."
            )
        }

        val lowerInput = input.lowercase()

        // 1. Check for Current Location Request
        if (lowerInput.contains("my location") || lowerInput.contains("here") || lowerInput.contains("current location")) {
            val coords = deviceCapabilities?.currentLocation()
            if (coords != null) {
                return when (val result = weatherRepository.getWeatherForCoordinates(coords.latitude, coords.longitude)) {
                    is WeatherResult.Success -> {
                        val speechText = formatWeatherAdvice("your current location", result.snapshot)
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

        // 2. Extract City Name from Input
        val targetCity = extractCityName(input)
        if (targetCity != null) {
            return when (val result = weatherRepository.getWeatherForCity(targetCity)) {
                is WeatherResult.Success -> {
                    val speechText = formatWeatherAdvice(targetCity, result.snapshot)
                    speakIfSupported(speechText)
                    AgentResponse(text = speechText, action = AgentAction.SearchWeather(targetCity))
                }
                is WeatherResult.Failure -> {
                    AgentResponse(text = "Sorry, I couldn't find weather data for '$targetCity'.")
                }
            }
        }

        // 3. Fallback / General Advice
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

    private fun formatWeatherAdvice(location: String, snapshot: WeatherSnapshot): String {
        val tempF = snapshot.temperature.toInt()
        val conditionDesc = snapshot.conditions.firstOrNull()?.description ?: "clear skies"
        val isRainy = conditionDesc.contains("rain") || conditionDesc.contains("drizzle")
        val isCold = tempF < 55

        val clothingAdvice = when {
            isRainy && isCold -> "Bring an umbrella and a waterproof jacket!"
            isRainy -> "Bring an umbrella!"
            isCold -> "You should wear a warm coat."
            tempF > 80 -> "It's warm, dress lightly!"
            else -> "Comfortable weather for outdoors!"
        }

        return "In ${snapshot.city}, it's currently $tempF°F with $conditionDesc. $clothingAdvice"
    }
}

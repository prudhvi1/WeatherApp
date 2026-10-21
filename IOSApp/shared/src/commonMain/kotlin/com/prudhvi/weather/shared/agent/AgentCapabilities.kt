package com.prudhvi.weather.shared.agent

import com.prudhvi.weather.shared.weather.CurrentLocationProvider

/**
 * Hardware access is represented as a capability so the agent never depends
 * directly on Android or iOS APIs. Each platform supplies its own adapter.
 */
interface DeviceCapabilities : CurrentLocationProvider {
    suspend fun speak(text: String)
    suspend fun openAppSettings()
}

data class AgentRequest(val text: String)

data class AgentResponse(
    val text: String,
    val action: AgentAction? = null
)

sealed interface AgentAction {
    data object FetchCurrentLocationWeather : AgentAction
    data class SearchWeather(val city: String) : AgentAction
}

interface WeatherAgent {
    suspend fun respond(request: AgentRequest): AgentResponse
}

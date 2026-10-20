package com.prudhvi.weather.presentation.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    onSearch: (String) -> Unit
) {
    var city by remember { mutableStateOf("Chicago") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Weather", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("US city") },
            singleLine = true
        )

        Button(
            onClick = { onSearch(city) },
            enabled = uiState !is WeatherUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Search")
        }

        when (val state = uiState) {
            WeatherUiState.Loading -> {
                CircularProgressIndicator()
            }

            is WeatherUiState.Error -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is WeatherUiState.Success -> {
                val response = state.weather
                val condition = response.weather.firstOrNull()
                Text(response.name, style = MaterialTheme.typography.headlineSmall)
                Text("${response.main.temp.toInt()}°F")
                Text(condition?.description.orEmpty().replaceFirstChar { it.uppercase() })
                Text("Feels like ${response.main.feels_like.toInt()}°F")
                Text("Humidity ${response.main.humidity}%")
            }
        }
    }
}

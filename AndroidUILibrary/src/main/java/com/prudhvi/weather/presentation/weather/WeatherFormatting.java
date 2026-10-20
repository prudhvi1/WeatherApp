package com.prudhvi.weather.presentation.weather;

import java.util.Locale;

/**
 * Java formatting helpers shared by the weather presentation layer.
 */
public final class WeatherFormatting {
    private WeatherFormatting() {
        // Utility class.
    }

    public static String formatTemperature(double temperature) {
        return String.format(Locale.US, "%d°F", Math.round(temperature));
    }

    public static String formatCondition(String description) {
        if (description == null || description.isEmpty()) {
            return "Unknown conditions";
        }

        return Character.toUpperCase(description.charAt(0)) + description.substring(1);
    }
}

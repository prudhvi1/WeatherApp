package com.prudhvi.weather.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(context: Context) {
    private val context = context.applicationContext
    private val locationManager =
        this.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resumeWithException(LocationPermissionException())
            return@suspendCancellableCoroutine
        }

        val provider = when {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ->
                LocationManager.NETWORK_PROVIDER
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ->
                LocationManager.GPS_PROVIDER
            else -> {
                continuation.resumeWithException(LocationUnavailableException())
                return@suspendCancellableCoroutine
            }
        }

        val lastKnownLocation = runCatching {
            locationManager.getLastKnownLocation(provider)
        }.getOrNull()

        if (lastKnownLocation != null) {
            continuation.resume(lastKnownLocation)
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }
        }

        try {
            locationManager.requestLocationUpdates(
                provider,
                MIN_TIME_MILLIS,
                MIN_DISTANCE_METERS,
                listener,
                Looper.getMainLooper()
            )
        } catch (exception: SecurityException) {
            continuation.resumeWithException(LocationPermissionException())
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        const val MIN_TIME_MILLIS = 10_000L
        const val MIN_DISTANCE_METERS = 10f
    }
}

class LocationPermissionException : Exception("Location permission is required")

class LocationUnavailableException : Exception("No location provider is enabled")

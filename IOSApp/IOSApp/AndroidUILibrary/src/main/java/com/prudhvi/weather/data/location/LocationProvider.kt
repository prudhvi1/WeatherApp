package com.prudhvi.weather.data.location

import android.Manifest
import android.annotation.SuppressLint
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

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

interface CurrentLocationProvider {
    suspend fun getCurrentLocation(): UserLocation
}

class LocationProvider(context: Context) : CurrentLocationProvider {
    private val context = context.applicationContext
    private val locationManager =
        this.context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): UserLocation = suspendCancellableCoroutine { continuation ->
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
            continuation.resume(
                UserLocation(
                    latitude = lastKnownLocation.latitude,
                    longitude = lastKnownLocation.longitude
                )
            )
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) {
                    continuation.resume(
                        UserLocation(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                    )
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
        } catch (_: SecurityException) {
            continuation.resumeWithException(LocationPermissionException())
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    private companion object {
        const val MIN_TIME_MILLIS = 10_000L
        const val MIN_DISTANCE_METERS = 10f
    }
}

class LocationPermissionException : Exception("Location permission is required")

class LocationUnavailableException : Exception("No location provider is enabled")

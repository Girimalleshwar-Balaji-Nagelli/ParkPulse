package com.parkpulse.app.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

interface LocationClient {
    /** Emits the last known fix (if any) then live updates. Throws SecurityException if permission is missing. */
    fun locationUpdates(intervalMs: Long = 2_000L): Flow<Location>
}

@Singleton
class FusedLocationClient @Inject constructor(
    @ApplicationContext context: Context,
) : LocationClient {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override fun locationUpdates(intervalMs: Long): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { trySend(it) }
            }
        }

        client.lastLocation.addOnSuccessListener { it?.let { loc -> trySend(loc) } }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())

        awaitClose { client.removeLocationUpdates(callback) }
    }
}

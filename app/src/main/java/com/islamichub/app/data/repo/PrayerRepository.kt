package com.islamichub.app.data.repo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.islamichub.app.data.model.PrayerTimes
import com.islamichub.app.data.remote.AladhanApi
import com.islamichub.app.data.remote.AladhanResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

/**
 * Prayer-times repository. Fetches via Aladhan API when location is available,
 * otherwise falls back to a deterministic default (Makkah coordinates) so the
 * UI never shows blank.
 */
class PrayerRepository(
    private val api: AladhanApi,
    private val context: Context
) {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            val loc = client.lastLocation.await()
            if (loc != null) return@withContext loc
            // Try to fetch a fresh location
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double,
        timestamp: Long = System.currentTimeMillis() / 1000
    ): Result<PrayerTimes> = withContext(Dispatchers.IO) {
        try {
            val response: AladhanResponse = api.getTimings(
                timestamp = timestamp,
                latitude = latitude,
                longitude = longitude
            )
            if (response.code != 200) {
                Result.failure(IllegalStateException("Aladhan code=${response.code}"))
            } else {
                val t = response.data.timings
                val d = response.data.date
                Result.success(
                    PrayerTimes(
                        fajr = t.Fajr,
                        sunrise = t.Sunrise,
                        dhuhr = t.Dhuhr,
                        asr = t.Asr,
                        maghrib = t.Maghrib,
                        isha = t.Isha,
                        date = d.readable,
                        hijriDate = "${d.hijri.day} ${d.hijri.month.en} ${d.hijri.year} AH",
                        locationName = "%.2f, %.2f".format(latitude, longitude)
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Default fallback — Makkah coordinates. */
    suspend fun getDefaultPrayerTimes(): Result<PrayerTimes> =
        getPrayerTimes(latitude = 21.4225, longitude = 39.8262)

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationEnabled(): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

package com.islamichub.app.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Aladhan API — free public prayer-times API. No key required. */
interface AladhanApi {

    @GET("timings/{timestamp}")
    suspend fun getTimings(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2, // ISNA
        @Query("school") school: Int = 0
    ): AladhanResponse

    @GET("calendar/{year}/{month}")
    suspend fun getCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 2
    ): AladhanCalendarResponse

    companion object {
        const val BASE_URL = "https://api.aladhan.com/v1/"
    }
}

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: AladhanTimingsData
)

data class AladhanTimingsData(
    val timings: AladhanTimings,
    val date: AladhanDate
)

data class AladhanTimings(
    val Fajr: String,
    val Sunrise: String,
    val Dhuhr: String,
    val Asr: String,
    val Maghrib: String,
    val Isha: String
)

data class AladhanDate(
    val readable: String,
    val timestamp: String,
    val hijri: AladhanHijriDate
)

data class AladhanHijriDate(
    val date: String,
    val day: String,
    val month: AladhanHijriMonth,
    val year: String,
    val weekday: AladhanHijriWeekday
)

data class AladhanHijriMonth(
    val number: Int,
    val en: String,
    val ar: String
)

data class AladhanHijriWeekday(
    val en: String,
    val ar: String
)

data class AladhanCalendarResponse(
    val code: Int,
    val status: String,
    val data: List<AladhanTimingsData>
)

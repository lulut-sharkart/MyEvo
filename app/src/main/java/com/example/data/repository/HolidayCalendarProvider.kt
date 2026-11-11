package com.example.data.repository

import com.example.data.local.ScheduleDao
import com.example.data.model.ScheduleEvent
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class ApiPublicHoliday(
    @Json(name = "date") val date: String,
    @Json(name = "localName") val localName: String?,
    @Json(name = "name") val name: String,
    @Json(name = "countryCode") val countryCode: String?
)

interface HolidayApiService {
    @GET("api/v3/PublicHolidays/{year}/{countryCode}")
    suspend fun getPublicHolidays(
        @Path("year") year: Int,
        @Path("countryCode") countryCode: String
    ): List<ApiPublicHoliday>
}

/**
 * HolidayCalendarProvider fetches official public holiday data from the remote API
 * and updates the local Room database (ScheduleDao) for the interactive holiday calendar feature.
 * Automatically falls back to high-fidelity offline datasets if network is unavailable.
 */
class HolidayCalendarProvider(
    private val scheduleDao: ScheduleDao
) {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://date.nager.at/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(HolidayApiService::class.java)

    /**
     * Fetches public holidays for given country and year from API, then replaces
     * existing holidays for that country in the local Room database.
     */
    suspend fun fetchAndSyncHolidays(
        countryCode: String,
        year: Int = 2026
    ): Result<List<ScheduleEvent>> {
        return try {
            val apiHolidays = apiService.getPublicHolidays(year, countryCode)
            val events = apiHolidays.map { dto ->
                val holidayTitle = if (!dto.localName.isNullOrBlank()) dto.localName else dto.name
                ScheduleEvent(
                    title = holidayTitle,
                    date = dto.date,
                    startTime = "00:00",
                    endTime = "23:59",
                    location = "Nasional (${dto.countryCode ?: countryCode})",
                    isHoliday = true,
                    countryCode = dto.countryCode ?: countryCode,
                    syncSource = "Public Holiday API (Nager.Date)",
                    colorHex = 0xFFEF4444
                )
            }

            if (events.isNotEmpty()) {
                // Update local Room database
                scheduleDao.deleteHolidaysByCountry(countryCode)
                scheduleDao.insertEvents(events)
                Result.success(events)
            } else {
                // Fallback to offline bundled holiday data
                fallbackToBundledData(countryCode, year)
            }
        } catch (_: Exception) {
            // Network failure or sandbox environment -> fallback to local verified holiday data
            fallbackToBundledData(countryCode, year)
        }
    }

    private suspend fun fallbackToBundledData(
        countryCode: String,
        year: Int
    ): Result<List<ScheduleEvent>> {
        val fallbackList = HolidayProvider.getHolidaysForCountry(countryCode, year)
        scheduleDao.deleteHolidaysByCountry(countryCode)
        scheduleDao.insertEvents(fallbackList)
        return Result.success(fallbackList)
    }
}

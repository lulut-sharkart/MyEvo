package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class SupabaseClientConfig(
    val baseUrl: String,
    val publishableKey: String,
    val isConfigured: Boolean,
    val maskedKeyPreview: String
)

/**
 * Singleton Supabase HTTP/REST client initialized from BuildConfig fields
 * populated via .env / .env.example (AI Studio Secrets Panel).
 */
object SupabaseClient {
    private const val TAG = "SupabaseClient"
    private const val DEFAULT_TIMEOUT_MS = 15_000L

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(12, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun getConfig(
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): SupabaseClientConfig {
        val cleanUrl = rawUrl.trim().trimEnd('/')
        val cleanKey = rawPublishableKey.trim()

        val hasValidUrl = cleanUrl.startsWith("https://") &&
            !cleanUrl.contains("YOUR_SUPABASE", ignoreCase = true) &&
            !cleanUrl.contains("YOUR_PROJECT_REF", ignoreCase = true) &&
            cleanUrl.length > "https://x.co".length

        val hasValidKey = cleanKey.isNotBlank() &&
            !cleanKey.contains("YOUR_SUPABASE", ignoreCase = true) &&
            cleanKey.length >= 16

        val masked = if (hasValidKey) {
            "${cleanKey.take(15)}••••"
        } else {
            "Belum diatur di Secrets"
        }

        return SupabaseClientConfig(
            baseUrl = cleanUrl,
            publishableKey = cleanKey,
            isConfigured = hasValidUrl && hasValidKey,
            maskedKeyPreview = masked
        )
    }

    fun restEndpoint(
        table: String,
        rawUrl: String = BuildConfig.SUPABASE_URL
    ): String {
        val base = rawUrl.trim().trimEnd('/')
        return "$base/rest/v1/$table"
    }

    fun storageObjectEndpoint(
        bucket: String,
        objectPath: String,
        rawUrl: String = BuildConfig.SUPABASE_URL
    ): String {
        val base = rawUrl.trim().trimEnd('/')
        return "$base/storage/v1/object/$bucket/$objectPath"
    }

    fun storagePublicEndpoint(
        bucket: String,
        objectPath: String,
        rawUrl: String = BuildConfig.SUPABASE_URL
    ): String {
        val base = rawUrl.trim().trimEnd('/')
        return "$base/storage/v1/object/public/$bucket/$objectPath"
    }

    fun authorizedRequestBuilder(
        url: String,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): Request.Builder {
        val cleanKey = rawPublishableKey.trim()
        return Request.Builder()
            .url(url)
            .addHeader("apikey", cleanKey)
            .addHeader("Authorization", "Bearer $cleanKey")
    }

    suspend fun pingRestEndpoint(
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): Result<String> {
        val config = getConfig(rawUrl, rawPublishableKey)
        if (!config.isConfigured) {
            return Result.failure(
                IllegalStateException(
                    "SUPABASE_URL & SUPABASE_PUBLISHABLE_KEY belum diatur di panel Secrets AI Studio."
                )
            )
        }

        return try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    val request = authorizedRequestBuilder(
                        url = "${config.baseUrl}/rest/v1/",
                        rawPublishableKey = config.publishableKey
                    ).get().build()

                    httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            Result.success("Koneksi SupabaseClient aktif (HTTP ${response.code}).")
                        } else {
                            Result.failure(
                                IllegalStateException(
                                    "Supabase merespons HTTP ${response.code}: periksa SUPABASE_URL dan SUPABASE_PUBLISHABLE_KEY."
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Supabase ping failed", e)
            Result.failure(e)
        }
    }
}

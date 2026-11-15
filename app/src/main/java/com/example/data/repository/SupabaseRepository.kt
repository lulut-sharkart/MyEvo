package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.BadgeEntity
import com.example.data.model.DebtLoanItem
import com.example.data.model.FinanceItem
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.data.remote.SupabaseClient
import com.example.data.remote.SupabaseService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data class SupabaseRemoteRecord(
    val id: String,
    val userName: String,
    val level: Int,
    val currentXp: Int,
    val streakDays: Int,
    val unlockedBadgesCount: Int,
    val updatedAtMs: Long
)

/**
 * Simple repository wrapping SupabaseClient to fetch or push initial MyEvo records
 * via Supabase PostgREST endpoints.
 */
class SupabaseRepository(
    private val rawUrl: String = BuildConfig.SUPABASE_URL,
    private val rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
) {
    companion object {
        private const val TAG = "SupabaseRepository"
        private const val TABLE_SNAPSHOTS = "myevo_sync_snapshots"
        private const val TIMEOUT_MS = 15_000L

        /**
         * Pure-Kotlin JSON extractor for Supabase snapshot rows so unit tests run
         * reliably on JVM without Android org.json stubs.
         */
        fun parseSnapshotRecordsJson(jsonArrayText: String): List<SupabaseRemoteRecord> {
            val trimmed = jsonArrayText.trim()
            if (!trimmed.startsWith("[") || trimmed == "[]") return emptyList()

            val objectMatches = Regex("\\{[^{}]*\"id\"\\s*:\\s*\"[^\"]*\"[^{}]*\\}").findAll(trimmed)
            return objectMatches.mapNotNull { match ->
                val obj = match.value
                val id = extractStringField(obj, "id") ?: return@mapNotNull null
                val userName = extractStringField(obj, "user_name") ?: "Pengguna MyEvo"
                val level = extractNumberField(obj, "level")?.toInt() ?: 1
                val currentXp = extractNumberField(obj, "current_xp")?.toInt() ?: 0
                val streakDays = extractNumberField(obj, "streak_days")?.toInt() ?: 0
                val badgesCount = extractNumberField(obj, "unlocked_badges_count")?.toInt() ?: 0
                val updatedAtMs = extractNumberField(obj, "updated_at_ms")?.toLong() ?: 0L
                SupabaseRemoteRecord(
                    id = id,
                    userName = userName,
                    level = level,
                    currentXp = currentXp,
                    streakDays = streakDays,
                    unlockedBadgesCount = badgesCount,
                    updatedAtMs = updatedAtMs
                )
            }.toList()
        }

        private fun extractStringField(obj: String, field: String): String? {
            val regex = Regex("\"$field\"\\s*:\\s*\"([^\"]*)\"")
            return regex.find(obj)?.groupValues?.getOrNull(1)
        }

        private fun extractNumberField(obj: String, field: String): Double? {
            val regex = Regex("\"$field\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
            return regex.find(obj)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
        }
    }

    suspend fun fetchInitialRecords(limit: Int = 20): Result<List<SupabaseRemoteRecord>> {
        val cfg = SupabaseClient.getConfig(rawUrl, rawPublishableKey)
        if (!cfg.isConfigured) {
            return Result.failure(
                IllegalStateException(
                    "SUPABASE_URL & SUPABASE_PUBLISHABLE_KEY belum diatur pada panel Secrets."
                )
            )
        }

        val url = "${SupabaseClient.restEndpoint(TABLE_SNAPSHOTS, rawUrl)}" +
            "?select=id,user_name,level,current_xp,streak_days,unlocked_badges_count,updated_at_ms" +
            "&order=updated_at_ms.desc&limit=$limit"

        return try {
            withTimeout(TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    val request = SupabaseClient.authorizedRequestBuilder(url, rawPublishableKey)
                        .get()
                        .addHeader("Accept", "application/json")
                        .build()

                    SupabaseClient.httpClient.newCall(request).execute().use { response ->
                        val body = response.body?.string().orEmpty()
                        if (response.isSuccessful) {
                            Result.success(parseSnapshotRecordsJson(body))
                        } else {
                            Result.failure(
                                IllegalStateException(
                                    "Gagal mengambil data Supabase (HTTP ${response.code}): ${body.take(160)}"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "fetchInitialRecords failed", e)
            Result.failure(e)
        }
    }

    suspend fun pushInitialRecords(
        profile: UserProfile,
        tasks: List<TaskItem>,
        finances: List<FinanceItem>,
        debts: List<DebtLoanItem>,
        badges: List<BadgeEntity>
    ): Result<Int> {
        return SupabaseService.syncSnapshotToSupabase(
            profile = profile,
            tasks = tasks,
            finances = finances,
            debts = debts,
            badges = badges,
            rawUrl = rawUrl,
            rawPublishableKey = rawPublishableKey
        )
    }
}

package com.example.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.BadgeEntity
import com.example.data.model.DebtLoanItem
import com.example.data.model.FinanceItem
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

data class SupabaseConfigStatus(
    val isConfigured: Boolean,
    val normalizedBaseUrl: String,
    val maskedKeyPreview: String
)

object SupabaseService {
    private const val TAG = "SupabaseService"
    private const val REQUEST_TIMEOUT_MS = 15_000L
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private val JPEG_MEDIA_TYPE = "image/jpeg".toMediaType()

    fun getConfigStatus(
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): SupabaseConfigStatus {
        val cfg = SupabaseClient.getConfig(rawUrl, rawPublishableKey)
        return SupabaseConfigStatus(
            isConfigured = cfg.isConfigured,
            normalizedBaseUrl = cfg.baseUrl,
            maskedKeyPreview = cfg.maskedKeyPreview
        )
    }

    fun escapeJson(value: String): String {
        val sb = StringBuilder(value.length + 8)
        for (ch in value) {
            when (ch) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> if (ch.code < 0x20) sb.append(String.format("\\u%04x", ch.code)) else sb.append(ch)
            }
        }
        return "\"$sb\""
    }

    fun buildSnapshotJson(
        snapshotId: String,
        profile: UserProfile,
        tasks: List<TaskItem>,
        finances: List<FinanceItem>,
        debts: List<DebtLoanItem>,
        badges: List<BadgeEntity>
    ): String {
        val tasksJson = tasks.take(50).joinToString(",", prefix = "[", postfix = "]") { task ->
            """{"id":${task.id},"title":${escapeJson(task.title.take(120))},"category":${escapeJson(task.category.name)},"priority":${escapeJson(task.priority.name)},"due_date":${escapeJson(task.dueDate)},"is_completed":${task.isCompleted}}"""
        }

        val financeJson = finances.take(50).joinToString(",", prefix = "[", postfix = "]") { fin ->
            """{"id":${fin.id},"type":${escapeJson(fin.type.name)},"category":${escapeJson(fin.category.name)},"amount":${fin.amount},"note":${escapeJson(fin.note.take(200))},"date":${escapeJson(fin.date)}}"""
        }

        val debtsJson = debts.take(50).joinToString(",", prefix = "[", postfix = "]") { debt ->
            """{"id":${debt.id},"type":${escapeJson(debt.type.name)},"person_name":${escapeJson(debt.personName.take(80))},"amount":${debt.amount},"paid_amount":${debt.effectivePaidAmount},"is_settled":${debt.isSettled},"due_date":${escapeJson(debt.dueDate)}}"""
        }

        val unlockedBadges = badges.count { it.isUnlocked }
        val userName = profile.name.ifBlank { "Pengguna MyEvo" }
        val updatedAtMs = System.currentTimeMillis()

        return """{"id":${escapeJson(snapshotId)},"user_name":${escapeJson(userName)},"user_email":${escapeJson(profile.emailOrPhone)},"level":${profile.level},"current_xp":${profile.currentXp},"streak_days":${profile.streakDays},"unlocked_badges_count":$unlockedBadges,"tasks_json":$tasksJson,"finances_json":$financeJson,"debts_json":$debtsJson,"updated_at_ms":$updatedAtMs}"""
    }

    suspend fun testConnection(
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): Result<String> = SupabaseClient.pingRestEndpoint(rawUrl, rawPublishableKey)

    suspend fun syncSnapshotToSupabase(
        profile: UserProfile,
        tasks: List<TaskItem>,
        finances: List<FinanceItem>,
        debts: List<DebtLoanItem>,
        badges: List<BadgeEntity>,
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): Result<Int> {
        val status = getConfigStatus(rawUrl, rawPublishableKey)
        if (!status.isConfigured) {
            return Result.failure(
                IllegalStateException(
                    "Lengkapi SUPABASE_URL dan SUPABASE_PUBLISHABLE_KEY pada panel Secrets AI Studio terlebih dahulu."
                )
            )
        }

        val snapshotId = profile.emailOrPhone
            .ifBlank { "myevo_default_user" }
            .replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
            .take(64)

        val payloadJson = buildSnapshotJson(
            snapshotId = snapshotId,
            profile = profile,
            tasks = tasks,
            finances = finances,
            debts = debts,
            badges = badges
        )

        return try {
            withTimeout(REQUEST_TIMEOUT_MS) {
                withContext(Dispatchers.IO) {
                    val request = SupabaseClient.authorizedRequestBuilder(
                        url = SupabaseClient.restEndpoint("myevo_sync_snapshots", rawUrl),
                        rawPublishableKey = rawPublishableKey
                    )
                        .post(payloadJson.toRequestBody(JSON_MEDIA_TYPE))
                        .addHeader("Content-Type", "application/json")
                        .addHeader("Prefer", "resolution=merge-duplicates,return=minimal")
                        .build()

                    SupabaseClient.httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val totalRecords = tasks.size + finances.size + debts.size + badges.size
                            Result.success(totalRecords)
                        } else {
                            val bodyPreview = response.body?.string()?.take(180).orEmpty()
                            Result.failure(
                                IllegalStateException(
                                    "Supabase HTTP ${response.code} pada tabel myevo_sync_snapshots. $bodyPreview"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Supabase snapshot sync failed", e)
            Result.failure(e)
        }
    }

    suspend fun uploadPhotoToSupabaseStorage(
        context: Context,
        imageUri: Uri,
        fileName: String,
        rawUrl: String = BuildConfig.SUPABASE_URL,
        rawPublishableKey: String = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ): Result<String> {
        val status = getConfigStatus(rawUrl, rawPublishableKey)
        if (!status.isConfigured) {
            return Result.failure(
                IllegalStateException(
                    "SUPABASE_URL & SUPABASE_PUBLISHABLE_KEY belum diatur di panel Secrets AI Studio."
                )
            )
        }

        val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9._\\-]"), "_")
        return try {
            withTimeout(20_000L) {
                withContext(Dispatchers.IO) {
                    val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                        ?: return@withContext Result.failure(IllegalStateException("Berkas foto tidak dapat dibaca."))

                    val uploadUrl = SupabaseClient.storageObjectEndpoint("myevo-photos", safeFileName, rawUrl)
                    val request = SupabaseClient.authorizedRequestBuilder(uploadUrl, rawPublishableKey)
                        .post(bytes.toRequestBody(JPEG_MEDIA_TYPE))
                        .addHeader("x-upsert", "true")
                        .build()

                    SupabaseClient.httpClient.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val publicUrl = SupabaseClient.storagePublicEndpoint("myevo-photos", safeFileName, rawUrl)
                            Result.success(publicUrl)
                        } else {
                            val bodyPreview = response.body?.string()?.take(180).orEmpty()
                            Result.failure(
                                IllegalStateException(
                                    "Gagal mengunggah ke bucket myevo-photos (HTTP ${response.code}): $bodyPreview"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Supabase photo upload failed", e)
            Result.failure(e)
        }
    }
}

package com.example.data.repository

import com.example.data.local.BadgeDao
import com.example.data.local.UserStreakDao
import com.example.data.model.BadgeEntity
import com.example.data.model.UserStreakEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BadgeProgressHelper {
    const val KEY_LANGUAGE_STREAK = "language_streak"
    const val KEY_TASK_STREAK = "task_streak"

    val defaultBadges: List<BadgeEntity> = listOf(
        BadgeEntity(
            id = "badge_first_task",
            title = "Langkah Pertama",
            description = "Selesaikan 1 tugas pertamamu untuk memulai produktivitas.",
            iconEmoji = "🎯",
            category = "TASK",
            currentProgress = 0,
            targetProgress = 1,
            xpReward = 50
        ),
        BadgeEntity(
            id = "badge_task_5",
            title = "Penakluk Tugas",
            description = "Selesaikan 5 tugas dengan konsisten dan tepat waktu.",
            iconEmoji = "⚡",
            category = "TASK",
            currentProgress = 0,
            targetProgress = 5,
            xpReward = 120
        ),
        BadgeEntity(
            id = "badge_task_15",
            title = "Master Fokus",
            description = "Capai 15 tugas tuntas untuk membuktikan dedikasi kerja kerasmu.",
            iconEmoji = "🏆",
            category = "TASK",
            currentProgress = 0,
            targetProgress = 15,
            xpReward = 250
        ),
        BadgeEntity(
            id = "badge_vocab_5",
            title = "Kolektor Kosakata",
            description = "Latih dan kuasai minimal 5 sesi ulasan kosakata bahasa asing.",
            iconEmoji = "📚",
            category = "LANGUAGE",
            currentProgress = 0,
            targetProgress = 5,
            xpReward = 150
        ),
        BadgeEntity(
            id = "badge_lang_streak_3",
            title = "Api Streak 3 Hari",
            description = "Pertahankan streak belajar bahasa selama 3 hari berturut-turut.",
            iconEmoji = "🔥",
            category = "LANGUAGE",
            currentProgress = 0,
            targetProgress = 3,
            xpReward = 150
        ),
        BadgeEntity(
            id = "badge_lang_streak_7",
            title = "Poliglot Berdedikasi",
            description = "Pertahankan streak belajar bahasa selama 7 hari penuh.",
            iconEmoji = "🗣️",
            category = "LANGUAGE",
            currentProgress = 0,
            targetProgress = 7,
            xpReward = 300
        ),
        BadgeEntity(
            id = "badge_crossword_1",
            title = "Ahli Teka-Teki AI",
            description = "Selesaikan 1 teka-teki silang harian hingga 100% benar.",
            iconEmoji = "🧩",
            category = "LANGUAGE",
            currentProgress = 0,
            targetProgress = 1,
            xpReward = 150
        )
    )

    fun todayDateStr(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun previousDayDateStr(dateStr: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsed = runCatching { sdf.parse(dateStr) }.getOrNull() ?: return ""
        val cal = Calendar.getInstance()
        cal.time = parsed
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    /**
     * Pure calculation of updated streak given previous streak state and today's YYYY-MM-DD date.
     */
    fun calculateUpdatedStreak(
        existing: UserStreakEntity?,
        key: String,
        todayStr: String = todayDateStr(),
        incrementTotal: Int = 1
    ): UserStreakEntity {
        val currentTotal = (existing?.totalCompletedCount ?: 0) + incrementTotal
        if (existing == null || existing.lastActivityDate.isBlank()) {
            return UserStreakEntity(
                key = key,
                currentStreak = 1,
                longestStreak = maxOf(1, existing?.longestStreak ?: 0),
                totalCompletedCount = currentTotal,
                lastActivityDate = todayStr
            )
        }
        val yesterdayStr = previousDayDateStr(todayStr)
        val newStreak = when (existing.lastActivityDate) {
            todayStr -> existing.currentStreak.coerceAtLeast(1)
            yesterdayStr -> existing.currentStreak + 1
            else -> 1
        }
        return existing.copy(
            currentStreak = newStreak,
            longestStreak = maxOf(existing.longestStreak, newStreak),
            totalCompletedCount = currentTotal,
            lastActivityDate = todayStr
        )
    }

    /**
     * Pure badge evaluation helper returning Pair(updatedBadge, wasNewlyUnlocked).
     */
    fun applyProgress(badge: BadgeEntity, newProgress: Int, nowMillis: Long = System.currentTimeMillis()): Pair<BadgeEntity, Boolean> {
        val bounded = newProgress.coerceIn(0, badge.targetProgress)
        val nowUnlocked = bounded >= badge.targetProgress
        val newlyUnlocked = nowUnlocked && !badge.isUnlocked
        val updated = badge.copy(
            currentProgress = maxOf(badge.currentProgress, bounded),
            isUnlocked = badge.isUnlocked || nowUnlocked,
            unlockedAt = badge.unlockedAt ?: if (nowUnlocked) nowMillis else null
        )
        return updated to newlyUnlocked
    }

    suspend fun seedDefaultBadgesIfEmpty(badgeDao: BadgeDao, streakDao: UserStreakDao) {
        if (badgeDao.getBadgeCount() == 0) {
            badgeDao.upsertBadges(defaultBadges)
        }
        if (streakDao.getStreak(KEY_TASK_STREAK) == null) {
            streakDao.upsertStreak(UserStreakEntity(key = KEY_TASK_STREAK))
        }
        if (streakDao.getStreak(KEY_LANGUAGE_STREAK) == null) {
            streakDao.upsertStreak(UserStreakEntity(key = KEY_LANGUAGE_STREAK))
        }
    }

    suspend fun recordTaskCompleted(
        badgeDao: BadgeDao,
        streakDao: UserStreakDao,
        todayStr: String = todayDateStr()
    ): List<BadgeEntity> {
        seedDefaultBadgesIfEmpty(badgeDao, streakDao)
        val updatedStreak = calculateUpdatedStreak(
            existing = streakDao.getStreak(KEY_TASK_STREAK),
            key = KEY_TASK_STREAK,
            todayStr = todayStr,
            incrementTotal = 1
        )
        streakDao.upsertStreak(updatedStreak)

        val totalCompleted = updatedStreak.totalCompletedCount
        val unlockedNow = mutableListOf<BadgeEntity>()
        val taskBadgeIds = listOf("badge_first_task", "badge_task_5", "badge_task_15")
        for (id in taskBadgeIds) {
            val badge = badgeDao.getBadgeById(id) ?: continue
            val (updatedBadge, isNew) = applyProgress(badge, totalCompleted)
            badgeDao.upsertBadge(updatedBadge)
            if (isNew) unlockedNow.add(updatedBadge)
        }
        return unlockedNow
    }

    suspend fun recordLanguagePractice(
        badgeDao: BadgeDao,
        streakDao: UserStreakDao,
        vocabReviewDelta: Int = 1,
        crosswordSolved: Boolean = false,
        todayStr: String = todayDateStr()
    ): List<BadgeEntity> {
        seedDefaultBadgesIfEmpty(badgeDao, streakDao)
        val updatedStreak = calculateUpdatedStreak(
            existing = streakDao.getStreak(KEY_LANGUAGE_STREAK),
            key = KEY_LANGUAGE_STREAK,
            todayStr = todayStr,
            incrementTotal = vocabReviewDelta.coerceAtLeast(0)
        )
        streakDao.upsertStreak(updatedStreak)

        val unlockedNow = mutableListOf<BadgeEntity>()

        // Update vocab badge
        if (vocabReviewDelta > 0) {
            badgeDao.getBadgeById("badge_vocab_5")?.let { badge ->
                val (updated, isNew) = applyProgress(badge, updatedStreak.totalCompletedCount)
                badgeDao.upsertBadge(updated)
                if (isNew) unlockedNow.add(updated)
            }
        }

        // Update language streak badges
        listOf("badge_lang_streak_3", "badge_lang_streak_7").forEach { id ->
            badgeDao.getBadgeById(id)?.let { badge ->
                val (updated, isNew) = applyProgress(badge, updatedStreak.currentStreak)
                badgeDao.upsertBadge(updated)
                if (isNew) unlockedNow.add(updated)
            }
        }

        // Update crossword badge
        if (crosswordSolved) {
            badgeDao.getBadgeById("badge_crossword_1")?.let { badge ->
                val (updated, isNew) = applyProgress(badge, badge.currentProgress + 1)
                badgeDao.upsertBadge(updated)
                if (isNew) unlockedNow.add(updated)
            }
        }

        return unlockedNow
    }
}

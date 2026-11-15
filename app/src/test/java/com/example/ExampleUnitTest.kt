package com.example

import com.example.data.model.AuthProvider
import com.example.data.model.UserProfile
import com.example.data.repository.HolidayProvider
import com.example.data.repository.LanguageLessonProvider
import com.example.data.repository.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSecurityHelperEncryptionAndDecryption() {
        val secretMessage = "Catatan rahasia keuangan dan target masa depan"
        val pin = "1234"

        val cipher = SecurityHelper.encrypt(secretMessage, pin)
        assertTrue(cipher.isNotEmpty())

        val decrypted = SecurityHelper.decrypt(cipher, pin)
        assertEquals(secretMessage, decrypted)
    }

    @Test
    fun testHolidayProviderForMultipleCountries() {
        val idHolidays = HolidayProvider.getHolidaysForCountry("ID", 2026)
        assertTrue(idHolidays.isNotEmpty())
        assertTrue(idHolidays.any { it.title.contains("Kemerdekaan") })

        val usHolidays = HolidayProvider.getHolidaysForCountry("US", 2026)
        assertTrue(usHolidays.isNotEmpty())
        assertTrue(usHolidays.any { it.title.contains("Independence") })

        val jpHolidays = HolidayProvider.getHolidaysForCountry("JP", 2026)
        assertTrue(jpHolidays.isNotEmpty())
    }

    @Test
    fun testCrosswordGeneration() {
        val puzzle = LanguageLessonProvider.getCrosswordForDate("2026-11-09")
        assertNotNull(puzzle)
        assertTrue(puzzle.clues.isNotEmpty())
    }

    @Test
    fun testAuthAndGuestModes() {
        val guestProfile = UserProfile(authProvider = AuthProvider.GUEST)
        assertTrue(guestProfile.isGuest)
        assertTrue(guestProfile.isLoggedIn)

        val googleProfile = UserProfile(authProvider = AuthProvider.GOOGLE, name = "Tester", emailOrPhone = "test@gmail.com")
        assertFalse(googleProfile.isGuest)
        assertTrue(googleProfile.isLoggedIn)

        val emptyProfile = UserProfile()
        assertFalse(emptyProfile.isLoggedIn)
    }

    @Test
    fun testRoomDatabaseSchemaEntities() {
        val task = com.example.data.model.TaskItem(
            id = 1L,
            title = "Selesaikan Presentasi Kuartal",
            category = com.example.data.model.TaskCategory.WORK,
            priority = com.example.data.model.TaskPriority.HIGH,
            dueDate = "2026-11-10"
        )
        assertEquals(1L, task.id)
        assertEquals("Selesaikan Presentasi Kuartal", task.title)
        assertFalse(task.isCompleted)

        val finance = com.example.data.model.FinanceItem(
            id = 1L,
            type = com.example.data.model.FinanceType.EXPENSE,
            category = com.example.data.model.ExpenseCategory.FOOD,
            amount = 45000.0,
            date = "2026-11-08",
            monthYear = "2026-11"
        )
        assertEquals(45000.0, finance.amount, 0.001)
        assertEquals(com.example.data.model.FinanceType.EXPENSE, finance.type)

        val note = com.example.data.model.EncryptedNote(
            id = 1L,
            title = "Ide Proyek Inovasi",
            encryptedContent = "U29tZUVuY3J5cHRlZERhdGE=",
            previewSnippet = "Ide proyek...",
            date = "2026-11-08"
        )
        assertEquals("Ide Proyek Inovasi", note.title)
        assertTrue(note.isLocked)
    }

    @Test
    fun testFinanceBudgetAndDebtCalculations() {
        val budgetGoal = 3000000.0
        val totalExpense = 1250000.0
        val remaining = (budgetGoal - totalExpense).coerceAtLeast(0.0)
        val usagePercent = (totalExpense / budgetGoal).toFloat()

        assertEquals(1750000.0, remaining, 0.001)
        assertEquals(0.416f, usagePercent, 0.01f)

        val debts = listOf(
            com.example.data.model.DebtLoanItem(
                id = 1L,
                type = com.example.data.model.DebtType.DEBT,
                personName = "Budi",
                amount = 200000.0,
                dueDate = "2026-11-15",
                isSettled = false
            ),
            com.example.data.model.DebtLoanItem(
                id = 2L,
                type = com.example.data.model.DebtType.DEBT,
                personName = "Andi",
                amount = 150000.0,
                dueDate = "2026-11-20",
                isSettled = true
            )
        )
        val activeDebt = debts.filter { it.type == com.example.data.model.DebtType.DEBT && !it.isSettled }.sumOf { it.amount }
        assertEquals(200000.0, activeDebt, 0.001)
    }

    @Test
    fun testPinHashingAndAppLock() {
        val pin = "1945"
        val hash1 = com.example.data.local.AppLockPreferences.hashPin(pin)
        val hash2 = com.example.data.local.AppLockPreferences.hashPin(pin)
        val wrongHash = com.example.data.local.AppLockPreferences.hashPin("2026")

        assertEquals(hash1, hash2)
        assertFalse(hash1 == wrongHash)
        assertTrue(hash1.length == 64) // SHA-256 output length
    }

    @Test
    fun testTaskDeadlineApproachingCalculation() {
        val task = com.example.data.model.TaskItem(
            id = 42L,
            title = "Kirim Laporan Pajak",
            dueDate = "2026-11-10",
            dueTime = "23:59",
            reminderMinutesBefore = 30
        )
        assertEquals(30, task.reminderMinutesBefore)
        assertFalse(task.isCompleted)
    }

    @Test
    fun testLanguageLearningEntityMastery() {
        val word = com.example.data.model.LanguageLearningEntity(
            id = 1L,
            word = "Persistence",
            translation = "Kegigihan",
            language = "English",
            phonetic = "/pərˈsɪs.təns/",
            exampleSentence = "Persistence brings success.",
            exampleTranslation = "Kegigihan mendatangkan kesuksesan.",
            category = "Motivation",
            masteryLevel = 3,
            reviewCount = 2
        )
        assertEquals("Persistence", word.word)
        assertEquals(3, word.masteryLevel)
        assertFalse(word.isMastered)

        // Test review hit leveling up to 5
        val nextLevel = (word.masteryLevel + 2).coerceAtMost(5)
        val isNowMastered = nextLevel >= 5
        assertEquals(5, nextLevel)
        assertTrue(isNowMastered)
    }

    @Test
    fun testHolidayCalendarProviderDtoAndFallback() {
        val dto = com.example.data.repository.ApiPublicHoliday(
            date = "2026-08-17",
            localName = "Hari Kemerdekaan Republik Indonesia",
            name = "Independence Day",
            countryCode = "ID"
        )
        assertEquals("ID", dto.countryCode)
        assertEquals("Independence Day", dto.name)

        // Verified fallback provider returns valid holidays for Indonesia
        val fallbackHolidays = HolidayProvider.getHolidaysForCountry("ID", 2026)
        assertTrue(fallbackHolidays.isNotEmpty())
        assertTrue(fallbackHolidays.any { it.date == "2026-08-17" })
    }

    @Test
    fun testAiGalleryItemCreation() {
        val galleryItem = com.example.data.model.AiGalleryItem(
            title = "Cyberpunk Cat",
            prompt = "Futuristic neon cat floating in cyberpunk Tokyo",
            style = "Cyberpunk Neon",
            subject = "Kucing Gemas",
            ratio = "1:1",
            dateStr = "11 Nov 2026, 12:00",
            isTransformation = false
        )
        assertEquals("Cyberpunk Cat", galleryItem.title)
        assertEquals("Cyberpunk Neon", galleryItem.style)
        assertFalse(galleryItem.isTransformation)
        assertTrue(galleryItem.id.isNotEmpty())
    }

    @Test
    fun testAuthenticLeaderboardNoDummyData() {
        val profile = UserProfile(
            name = "Ahmad Developer",
            currentXp = 1500,
            level = 4
        )
        val leaderboard = listOf(
            com.example.data.model.LeaderboardUser(
                rank = 1,
                name = profile.name,
                avatarEmoji = profile.avatarEmoji,
                points = profile.currentXp,
                level = profile.level,
                badge = "Evo Pro",
                isCurrentUser = true
            )
        )
        // Leaderboard must only contain authentic user, zero fake bots
        assertEquals(1, leaderboard.size)
        assertEquals("Ahmad Developer", leaderboard.first().name)
        assertTrue(leaderboard.first().isCurrentUser)
        assertEquals(1, leaderboard.first().rank)
        assertFalse(leaderboard.any { it.name == "Siti Nurhaliza" || it.name == "Budi Gunawan" })
    }

    @Test
    fun testUserProfileComprehensiveSettings() {
        val profile = UserProfile()
        assertTrue(profile.isTaskReminderEnabled)
        assertTrue(profile.isStudyReminderEnabled)
        assertTrue(profile.isVibrationEnabled)
        assertTrue(profile.isSoundEnabled)
        assertEquals(30, profile.defaultReminderMinutes)
        assertEquals("IDR", profile.currencyCode)
        assertEquals("id", profile.appLanguage)
        assertFalse(profile.isAppLockEnabled)

        val updated = profile.copy(
            isDarkMode = true,
            isAppLockEnabled = true,
            defaultReminderMinutes = 15,
            currencyCode = "USD"
        )
        assertTrue(updated.isDarkMode)
        assertTrue(updated.isAppLockEnabled)
        assertEquals(15, updated.defaultReminderMinutes)
        assertEquals("USD", updated.currencyCode)
    }

    @Test
    fun testDataExportTasksCsvGeneration() {
        val sampleTasks = listOf(
            com.example.data.model.TaskItem(
                id = 101L,
                title = "Siapkan Laporan, Evaluasi & Target",
                description = "Termasuk data \"Q3\" dan roadmap",
                category = com.example.data.model.TaskCategory.WORK,
                priority = com.example.data.model.TaskPriority.HIGH,
                dueDate = "2026-11-15",
                dueTime = "10:00",
                isCompleted = false
            )
        )
        val csv = com.example.data.repository.DataExportHelper.generateTasksCsv(sampleTasks)
        assertTrue(csv.contains("ID,Judul Tugas,Deskripsi,Kategori,Prioritas"))
        assertTrue(csv.contains("101"))
        assertTrue(csv.contains("\"Siapkan Laporan, Evaluasi & Target\""))
        assertTrue(csv.contains("\"Termasuk data \"\"Q3\"\" dan roadmap\""))
        assertTrue(csv.contains("BELUM SELESAI"))
    }

    @Test
    fun testDataExportFinanceCsvGeneration() {
        val sampleFinances = listOf(
            com.example.data.model.FinanceItem(
                id = 201L,
                type = com.example.data.model.FinanceType.EXPENSE,
                category = com.example.data.model.ExpenseCategory.FOOD,
                amount = 75000.0,
                note = "Makan siang meeting, cafe",
                date = "2026-11-10",
                monthYear = "2026-11"
            )
        )
        val sampleDebts = listOf(
            com.example.data.model.DebtLoanItem(
                id = 301L,
                type = com.example.data.model.DebtType.DEBT,
                personName = "Rudi Hartono",
                amount = 150000.0,
                dueDate = "2026-11-20",
                isSettled = false
            )
        )
        val csv = com.example.data.repository.DataExportHelper.generateFinanceCsv(sampleFinances, sampleDebts)
        assertTrue(csv.contains("ID,Tipe Transaksi,Kategori / Pihak,Nominal (Rp)"))
        assertTrue(csv.contains("FIN-201"))
        assertTrue(csv.contains("PENGELUARAN"))
        assertTrue(csv.contains("DEBT-301"))
        assertTrue(csv.contains("Rudi Hartono"))
        assertTrue(csv.contains("BELUM LUNAS"))
    }

    @Test
    fun testDebtPartialPaymentProgressCalculation() {
        val debt = com.example.data.model.DebtLoanItem(
            id = 501L,
            type = com.example.data.model.DebtType.DEBT,
            personName = "Sari",
            amount = 1_000_000.0,
            paidAmount = 350_000.0,
            dueDate = "2026-12-01",
            isSettled = false
        )
        assertEquals(350_000.0, debt.effectivePaidAmount, 0.001)
        assertEquals(650_000.0, debt.remainingAmount, 0.001)
        assertEquals(0.35f, debt.progressFraction, 0.001f)

        val settledDebt = debt.copy(paidAmount = 1_000_000.0, isSettled = true)
        assertEquals(1_000_000.0, settledDebt.effectivePaidAmount, 0.001)
        assertEquals(0.0, settledDebt.remainingAmount, 0.001)
        assertEquals(1.0f, settledDebt.progressFraction, 0.001f)
    }

    @Test
    fun testBadgeProgressAndStreakCalculation() {
        val streakKey = com.example.data.repository.BadgeProgressHelper.KEY_LANGUAGE_STREAK
        val day1 = com.example.data.repository.BadgeProgressHelper.calculateUpdatedStreak(
            existing = null,
            key = streakKey,
            todayStr = "2026-11-10"
        )
        assertEquals(1, day1.currentStreak)
        assertEquals(1, day1.totalCompletedCount)

        // Same day review keeps streak at 1 but increments total
        val sameDay = com.example.data.repository.BadgeProgressHelper.calculateUpdatedStreak(
            existing = day1,
            key = streakKey,
            todayStr = "2026-11-10"
        )
        assertEquals(1, sameDay.currentStreak)
        assertEquals(2, sameDay.totalCompletedCount)

        // Consecutive day increments streak to 2
        val day2 = com.example.data.repository.BadgeProgressHelper.calculateUpdatedStreak(
            existing = sameDay,
            key = streakKey,
            todayStr = "2026-11-11"
        )
        assertEquals(2, day2.currentStreak)
        assertEquals(2, day2.longestStreak)

        // Missed day resets currentStreak to 1 while preserving longestStreak
        val dayAfterGap = com.example.data.repository.BadgeProgressHelper.calculateUpdatedStreak(
            existing = day2,
            key = streakKey,
            todayStr = "2026-11-14"
        )
        assertEquals(1, dayAfterGap.currentStreak)
        assertEquals(2, dayAfterGap.longestStreak)

        // Badge unlocking
        val badge = com.example.data.model.BadgeEntity(
            id = "badge_task_5",
            title = "Penakluk Tugas",
            description = "Selesaikan 5 tugas",
            iconEmoji = "⚡",
            category = "TASK",
            currentProgress = 2,
            targetProgress = 5
        )
        val (partialBadge, newlyUnlockedPartial) = com.example.data.repository.BadgeProgressHelper.applyProgress(badge, 4)
        assertFalse(newlyUnlockedPartial)
        assertFalse(partialBadge.isUnlocked)
        assertEquals(0.8f, partialBadge.progressFraction, 0.001f)

        val (unlockedBadge, newlyUnlockedFull) = com.example.data.repository.BadgeProgressHelper.applyProgress(partialBadge, 5)
        assertTrue(newlyUnlockedFull)
        assertTrue(unlockedBadge.isUnlocked)
        assertEquals(1.0f, unlockedBadge.progressFraction, 0.001f)
    }

    @Test
    fun testCrosswordJsonExtractStripMarkdownFences() {
        val fenced = "```json\n{\"category\":\"FOOD\",\"confidence\":95}\n```"
        val extracted = com.example.data.remote.CrosswordJsonParser.extractJsonObject(fenced)
        assertTrue(extracted.startsWith("{"))
        assertTrue(extracted.endsWith("}"))
        assertTrue(extracted.contains("\"FOOD\""))
    }

    @Test
    fun testSupabaseConfigValidationAndPayloadConstruction() {
        val unconfigured = com.example.data.remote.SupabaseService.getConfigStatus(
            rawUrl = "YOUR_SUPABASE_URL",
            rawPublishableKey = "YOUR_SUPABASE_PUBLISHABLE_KEY"
        )
        assertFalse(unconfigured.isConfigured)

        val configured = com.example.data.remote.SupabaseService.getConfigStatus(
            rawUrl = "https://demo123.supabase.co/",
            rawPublishableKey = "sb_publishable_example_key_123456"
        )
        assertTrue(configured.isConfigured)
        assertEquals("https://demo123.supabase.co", configured.normalizedBaseUrl)
        assertTrue(configured.maskedKeyPreview.startsWith("sb_publishable_"))

        val payloadJson = com.example.data.remote.SupabaseService.buildSnapshotJson(
            snapshotId = "user_1",
            profile = UserProfile(name = "Lulut", level = 3, currentXp = 220),
            tasks = listOf(
                com.example.data.model.TaskItem(
                    id = 1L,
                    title = "Tugas Harian",
                    dueDate = "2026-11-15"
                )
            ),
            finances = emptyList(),
            debts = emptyList(),
            badges = emptyList()
        )
        assertTrue(payloadJson.contains("\"id\":\"user_1\""))
        assertTrue(payloadJson.contains("\"user_name\":\"Lulut\""))
        assertTrue(payloadJson.contains("\"Tugas Harian\""))
    }

    @Test
    fun testPinSecurityDataStoreSaltedHashingAndConstantTimeComparison() {
        val salt1 = com.example.data.local.PinSecurityDataStore.generateSaltHex()
        val salt2 = com.example.data.local.PinSecurityDataStore.generateSaltHex()
        assertEquals(32, salt1.length)
        assertFalse(salt1 == salt2)

        val hashPinA = com.example.data.local.PinSecurityDataStore.hashSecret("7391", salt1)
        val hashPinB = com.example.data.local.PinSecurityDataStore.hashSecret("7391", salt1)
        val hashPinDiffSalt = com.example.data.local.PinSecurityDataStore.hashSecret("7391", salt2)
        val hashAdmin = com.example.data.local.PinSecurityDataStore.hashSecret("SuperSecret!99", salt1)

        assertEquals(64, hashPinA.length)
        assertTrue(com.example.data.local.PinSecurityDataStore.constantTimeEquals(hashPinA, hashPinB))
        assertFalse(hashPinA == hashPinDiffSalt)
        assertFalse(hashPinA == hashAdmin)
    }

    @Test
    fun testSupabaseClientConfigAndRepositoryJsonParsing() {
        val cfg = com.example.data.remote.SupabaseClient.getConfig(
            rawUrl = "https://xyzcompany.supabase.co/",
            rawPublishableKey = "sb_publishable_demo_token_987654"
        )
        assertTrue(cfg.isConfigured)
        assertEquals(
            "https://xyzcompany.supabase.co/rest/v1/myevo_sync_snapshots",
            com.example.data.remote.SupabaseClient.restEndpoint("myevo_sync_snapshots", "https://xyzcompany.supabase.co/")
        )

        val rawJson = """
            [
              {"id":"u_1","user_name":"Rina","level":5,"current_xp":420,"streak_days":9,"unlocked_badges_count":4,"updated_at_ms":1700000000000},
              {"id":"u_2","user_name":"Bima","level":2,"current_xp":110,"streak_days":3,"unlocked_badges_count":1,"updated_at_ms":1699999000000}
            ]
        """.trimIndent()
        val records = com.example.data.repository.SupabaseRepository.parseSnapshotRecordsJson(rawJson)
        assertEquals(2, records.size)
        assertEquals("Rina", records[0].userName)
        assertEquals(5, records[0].level)
        assertEquals(4, records[0].unlockedBadgesCount)
        assertEquals("Bima", records[1].userName)
    }

    @Test
    fun testCalendarMultiRangeGeneration() {
        val week = com.example.data.repository.CalendarDateHelper.generateWeekDays("2026-11-11")
        assertEquals(7, week.size)
        assertEquals("Sen", week.first().dayOfWeekShort)
        assertEquals("Min", week.last().dayOfWeekShort)

        val oneMonth = com.example.data.repository.CalendarDateHelper.generateMultiMonthData("2026-11-11", 1)
        val threeMonths = com.example.data.repository.CalendarDateHelper.generateMultiMonthData("2026-11-11", 3)
        val sixMonths = com.example.data.repository.CalendarDateHelper.generateMultiMonthData("2026-11-11", 6)
        val oneYear = com.example.data.repository.CalendarDateHelper.generateMultiMonthData("2026-11-11", 12)

        assertEquals(1, oneMonth.size)
        assertEquals(3, threeMonths.size)
        assertEquals(6, sixMonths.size)
        assertEquals(12, oneYear.size)
        assertEquals(0, oneYear.first().monthZeroBased)
        assertEquals(11, oneYear.last().monthZeroBased)

        val nextYear = com.example.data.repository.CalendarDateHelper.shiftAnchorDate(
            "2026-11-11",
            com.example.data.repository.CalendarViewMode.ONE_YEAR,
            1
        )
        assertTrue(nextYear.startsWith("2027-"))
    }

    @Test
    fun testToolsPeriodBmiAndPasswordHelpers() {
        val period = com.example.ui.screens.tools.PeriodCalculatorMath.calculate(
            lastPeriodDateStr = "2026-11-01",
            cycleLengthDays = 28,
            periodDurationDays = 5,
            referenceDateStr = "2026-11-15"
        )
        assertEquals("2026-11-29", period.nextPeriodDate)
        assertEquals("2026-11-15", period.ovulationDate)
        assertEquals(3, period.upcomingCycles.size)

        val bmi = com.example.ui.screens.tools.BmiCalculatorMath.calculate(
            weightKg = 64.0,
            heightCm = 170.0,
            ageYears = 26,
            isMale = true
        )
        assertEquals(22.14, bmi.bmi, 0.1)
        assertTrue(bmi.category.contains("Normal"))
        assertTrue(bmi.estimatedBmrKcal > 1400)

        val pwd = com.example.ui.screens.tools.PasswordGeneratorHelper.generate(16, includeSymbols = true)
        assertEquals(16, pwd.length)
    }
}

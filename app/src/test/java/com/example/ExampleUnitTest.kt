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
}

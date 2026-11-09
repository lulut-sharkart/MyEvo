package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    HIGH, MEDIUM, LOW
}

enum class TaskCategory {
    WORK, PERSONAL, STUDY, FINANCE, HEALTH
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: String, // YYYY-MM-DD
    val dueTime: String = "17:00", // HH:mm
    val isCompleted: Boolean = false,
    val reminderMinutesBefore: Int = 30, // 15, 30, 60
    val isSyncedWithGoogle: Boolean = false,
    val isCloudSynced: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val date: String, // YYYY-MM-DD
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val location: String = "",
    val isHoliday: Boolean = false,
    val countryCode: String = "ID",
    val syncSource: String = "MyEvo", // "Google Calendar", "Outlook", "MyEvo"
    val colorHex: Long = 0xFF6366F1
)

enum class FinanceType {
    INCOME, EXPENSE
}

enum class ExpenseCategory {
    FOOD, TRANSPORT, BILLS, SHOPPING, HEALTH, ENTERTAINMENT, SALARY, OTHER
}

@Entity(tableName = "finance_entries")
data class FinanceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: FinanceType,
    val category: ExpenseCategory,
    val amount: Double,
    val note: String = "",
    val date: String, // YYYY-MM-DD
    val monthYear: String, // YYYY-MM
    val createdAt: Long = System.currentTimeMillis()
)

enum class DebtType {
    DEBT, // Saya berutang ke orang
    LOAN  // Orang berutang ke saya (Piutang)
}

@Entity(tableName = "debts_loans")
data class DebtLoanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: DebtType,
    val personName: String,
    val amount: Double,
    val dueDate: String, // YYYY-MM-DD
    val note: String = "",
    val isSettled: Boolean = false,
    val contactPhone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "encrypted_notes")
data class EncryptedNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val encryptedContent: String, // Ciphertext base64
    val previewSnippet: String, // Hint or masked
    val date: String,
    val mood: String = "😊", // Emoji mood
    val tag: String = "Pribadi",
    val isLocked: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dictionary_words")
data class DictionaryWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val word: String,
    val translation: String,
    val language: String, // English, Japanese, Korean, German
    val exampleSentence: String,
    val pronunciation: String,
    val isBookmarked: Boolean = false,
    val category: String = "Daily"
)

data class CrosswordClue(
    val number: Int,
    val direction: String, // "ACROSS" or "DOWN"
    val clue: String,
    val answer: String,
    val startRow: Int,
    val startCol: Int
)

data class CrosswordPuzzle(
    val dayId: String, // YYYY-MM-DD
    val title: String,
    val topic: String,
    val gridSize: Int = 7,
    val clues: List<CrosswordClue>,
    val xpReward: Int = 150
)

enum class AuthProvider {
    NONE,
    GOOGLE,
    FACEBOOK,
    PHONE,
    GUEST
}

data class UserProfile(
    val authProvider: AuthProvider = AuthProvider.NONE,
    val name: String = "",
    val emailOrPhone: String = "",
    val title: String = "Evo Novice",
    val avatarEmoji: String = "👤",
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 500,
    val streakDays: Int = 1,
    val completedTasksCount: Int = 0,
    val monthlyBudgetGoal: Double = 3000000.0,
    val isDarkMode: Boolean = false,
    val isReadingMode: Boolean = false,
    val selectedCountry: String = "Indonesia (ID)",
    val countryCode: String = "ID",
    val isAutoSyncEnabled: Boolean = true,
    val isOnline: Boolean = true,
    val lastSyncTimestamp: String = "Belum sinkron",
    val activeBadges: List<String> = listOf("Evo Explorer")
) {
    val isGuest: Boolean get() = authProvider == AuthProvider.GUEST
    val isLoggedIn: Boolean get() = authProvider != AuthProvider.NONE
}

data class LeaderboardUser(
    val rank: Int,
    val name: String,
    val avatarEmoji: String,
    val points: Int,
    val level: Int,
    val badge: String,
    val isCurrentUser: Boolean = false
)

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val progress: Int,
    val rewardPoints: Int,
    val endsInDays: Int,
    val isClaimed: Boolean = false
)


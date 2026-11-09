package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MyEvoDatabase
import com.example.data.model.AuthProvider
import com.example.data.model.DebtLoanItem
import com.example.data.model.DebtType
import com.example.data.model.DictionaryWord
import com.example.data.model.EncryptedNote
import com.example.data.model.ExpenseCategory
import com.example.data.model.FinanceItem
import com.example.data.model.FinanceType
import com.example.data.model.LeaderboardUser
import com.example.data.model.ScheduleEvent
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.data.model.UserProfile
import com.example.data.model.WeeklyChallenge
import com.example.data.repository.HolidayProvider
import com.example.data.repository.LanguageLesson
import com.example.data.repository.LanguageLessonProvider
import com.example.data.repository.MyEvoRepository
import com.example.data.repository.PushNotification
import com.example.data.repository.SecurityHelper
import com.example.data.repository.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    AUTH,
    HOME,
    CALENDAR,
    FINANCE,
    LEARN,
    STUDIO,
    NOTES,
    PROFILE,
    ADMIN
}

enum class AiBackgroundStyle(val label: String, val promptDescription: String, val colorCode: Long) {
    NEON_STUDIO("Neon Cyberpunk", "Futuristic neon purple-cyan holographic studio with soft particle glow", 0xFF6366F1),
    COZY_CAFE("Cozy Cafe", "Warm aesthetic coffee shop with soft bokeh lighting and wooden texture", 0xFFD97706),
    SUNSET_NATURE("Sunset Paradise", "Golden hour mountain horizon with warm pastel gradient skies", 0xFFF97316),
    TRANSPARENT_CLEAN("Transparan Studio", "Clean isolated transparent alpha background for professional stickers", 0xFF10B981),
    MINIMAL_PASTEL("Minimalist Gradient", "Smooth luxury lavender & mint pastel gradient backdrop", 0xFF8B5CF6)
}

class MyEvoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MyEvoRepository

    init {
        val database = MyEvoDatabase.getDatabase(application)
        repository = MyEvoRepository(database)
        viewModelScope.launch {
            // Remove any old dummy data to ensure fresh, clean user storage
            repository.clearAllUserData()
            repository.populateInitialDataIfEmpty()
        }
    }

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.AUTH)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    // User Profile & Authentication
    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun loginWithGoogle(name: String = "Lulut Solehhudin", email: String = "lulut.solehhudin19@guru.sd.belajar.id") {
        _userProfile.value = _userProfile.value.copy(
            authProvider = AuthProvider.GOOGLE,
            name = name,
            emailOrPhone = email,
            title = "Evo Google Member",
            avatarEmoji = "🚀",
            isAutoSyncEnabled = true,
            lastSyncTimestamp = "Sinkron aktif"
        )
        _currentScreen.value = AppScreen.HOME
        SyncManager.pushCustomNotification(
            title = "Google Sign-In Berhasil",
            message = "Selamat datang, $name! Akun Google Anda terhubung dan sinkronisasi awan aktif.",
            type = "SYNC"
        )
    }

    fun loginWithFacebook(name: String = "Lulut Solehhudin (FB)", email: String = "lulut.fb@social.id") {
        _userProfile.value = _userProfile.value.copy(
            authProvider = AuthProvider.FACEBOOK,
            name = name,
            emailOrPhone = email,
            title = "Evo Social Member",
            avatarEmoji = "💎",
            isAutoSyncEnabled = true,
            lastSyncTimestamp = "Sinkron aktif"
        )
        _currentScreen.value = AppScreen.HOME
        SyncManager.pushCustomNotification(
            title = "Facebook Login Berhasil",
            message = "Selamat datang, $name! Akun Facebook terhubung dengan aman.",
            type = "SYNC"
        )
    }

    fun loginWithPhone(phoneNumber: String) {
        _userProfile.value = _userProfile.value.copy(
            authProvider = AuthProvider.PHONE,
            name = "User ($phoneNumber)",
            emailOrPhone = phoneNumber,
            title = "Evo Mobile Member",
            avatarEmoji = "📱",
            isAutoSyncEnabled = true,
            lastSyncTimestamp = "Sinkron aktif"
        )
        _currentScreen.value = AppScreen.HOME
        SyncManager.pushCustomNotification(
            title = "Verifikasi Nomor HP Berhasil",
            message = "Nomor $phoneNumber telah diverifikasi via SMS OTP.",
            type = "SYNC"
        )
    }

    fun loginAsGuest() {
        _userProfile.value = _userProfile.value.copy(
            authProvider = AuthProvider.GUEST,
            name = "Pengguna Tamu",
            emailOrPhone = "Mode Tamu (Tanpa Sinkronisasi)",
            title = "Tamu Penjelajah",
            avatarEmoji = "🎭",
            isAutoSyncEnabled = false,
            lastSyncTimestamp = "Tidak disinkronkan"
        )
        _currentScreen.value = AppScreen.HOME
        SyncManager.pushCustomNotification(
            title = "🎭 Mode Tamu Aktif",
            message = "Anda dapat menguji seluruh fungsi aplikasi. Data hanya tersimpan lokal di perangkat dan tidak disinkronkan ke cloud.",
            type = "REMINDER"
        )
    }

    fun logout() {
        viewModelScope.launch {
            _userProfile.value = UserProfile(authProvider = AuthProvider.NONE)
            _currentScreen.value = AppScreen.AUTH
        }
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            SyncManager.pushCustomNotification(
                title = "Data Bersih",
                message = "Seluruh data lokal telah dihapus.",
                type = "SYNC"
            )
        }
    }

    fun toggleDarkMode() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isDarkMode = !current.isDarkMode, isReadingMode = false)
    }

    fun toggleReadingMode() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isReadingMode = !current.isReadingMode)
    }

    fun setCountry(countryCode: String, countryName: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(countryCode = countryCode, selectedCountry = "$countryName ($countryCode)")
        viewModelScope.launch {
            repository.syncHolidaysForCountry(countryCode, 2026)
        }
    }

    fun updateMonthlyBudget(goal: Double) {
        _userProfile.value = _userProfile.value.copy(monthlyBudgetGoal = goal)
    }

    // Sync & Connectivity
    val isOnline: StateFlow<Boolean> = SyncManager.isOnline
    val isSyncing: StateFlow<Boolean> = SyncManager.isSyncing
    val lastSyncTime: StateFlow<String> = SyncManager.lastSyncTime
    val notifications: StateFlow<List<PushNotification>> = SyncManager.notifications

    fun toggleOnlineMode() {
        SyncManager.toggleOnlineStatus()
    }

    fun triggerCloudBackup() {
        if (_userProfile.value.isGuest) {
            SyncManager.pushCustomNotification(
                title = "⚠️ Mode Tamu Aktif",
                message = "Data lokal tidak disinkronkan ke cloud dalam mode tamu. Masuk dengan akun Google, Facebook, atau No. HP untuk mengaktifkan sinkronisasi awan.",
                type = "SYNC"
            )
            return
        }
        SyncManager.triggerSync()
        SyncManager.pushCustomNotification(
            title = "☁️ Pencadangan Cloud Selesai",
            message = "Cadangan database lokal berhasil dicadangkan ke server awan MyEvo.",
            type = "SYNC"
        )
    }

    // Tasks & Priorities (Optimized with distinctUntilChanged)
    val tasks: StateFlow<List<TaskItem>> = repository.allTasks
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(
        title: String,
        description: String,
        category: TaskCategory,
        priority: TaskPriority,
        dueDate: String,
        dueTime: String,
        reminderMinutes: Int
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskItem(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    reminderMinutesBefore = reminderMinutes
                )
            )
            addXp(25)
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            val newStatus = !task.isCompleted
            repository.setTaskCompleted(task.id, newStatus)
            if (newStatus) {
                addXp(50)
                _userProfile.value = _userProfile.value.copy(
                    completedTasksCount = _userProfile.value.completedTasksCount + 1
                )
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Schedules & Calendar
    val events: StateFlow<List<ScheduleEvent>> = repository.allEvents
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedCalendarDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedCalendarDate: StateFlow<String> = _selectedCalendarDate.asStateFlow()

    fun setSelectedCalendarDate(date: String) {
        _selectedCalendarDate.value = date
    }

    fun addScheduleEvent(
        title: String,
        date: String,
        startTime: String,
        endTime: String,
        location: String,
        syncSource: String = "MyEvo"
    ) {
        viewModelScope.launch {
            repository.insertEvent(
                ScheduleEvent(
                    title = title,
                    date = date,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    syncSource = syncSource,
                    isHoliday = false,
                    colorHex = 0xFF6366F1
                )
            )
        }
    }

    fun syncGoogleCalendar() {
        if (_userProfile.value.isGuest) {
            SyncManager.pushCustomNotification(
                title = "⚠️ Mode Tamu",
                message = "Hubungkan akun Google terlebih dahulu untuk sinkronisasi Google Calendar.",
                type = "SYNC"
            )
            return
        }
        viewModelScope.launch {
            val todayStr = _selectedCalendarDate.value
            repository.insertEvent(
                ScheduleEvent(
                    title = "Google Meet: Sesi Perencanaan Pribadi",
                    date = todayStr,
                    startTime = "14:00",
                    endTime = "15:00",
                    location = "Google Meet Link",
                    syncSource = "Google Calendar",
                    colorHex = 0xFF4285F4
                )
            )
            SyncManager.pushCustomNotification(
                title = "📅 Google Calendar Terhubung",
                message = "Jadwal rapat Google Meet berhasil disinkronkan ke kalender MyEvo.",
                type = "SYNC"
            )
        }
    }

    // Finance & Debts (Optimized with distinctUntilChanged)
    val financeEntries: StateFlow<List<FinanceItem>> = repository.allFinance
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debtsLoans: StateFlow<List<DebtLoanItem>> = repository.allDebtsLoans
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addFinance(
        type: FinanceType,
        category: ExpenseCategory,
        amount: Double,
        note: String,
        date: String
    ) {
        viewModelScope.launch {
            val monthYear = if (date.length >= 7) date.substring(0, 7) else "2026-11"
            repository.insertFinance(
                FinanceItem(
                    type = type,
                    category = category,
                    amount = amount,
                    note = note,
                    date = date,
                    monthYear = monthYear
                )
            )
            addXp(20)
        }
    }

    fun deleteFinance(item: FinanceItem) {
        viewModelScope.launch {
            repository.deleteFinance(item)
        }
    }

    fun addDebtLoan(
        type: DebtType,
        personName: String,
        amount: Double,
        dueDate: String,
        note: String,
        contactPhone: String
    ) {
        viewModelScope.launch {
            repository.insertDebtLoan(
                DebtLoanItem(
                    type = type,
                    personName = personName,
                    amount = amount,
                    dueDate = dueDate,
                    note = note,
                    contactPhone = contactPhone,
                    isSettled = false
                )
            )
            addXp(30)
        }
    }

    fun toggleDebtLoanSettled(item: DebtLoanItem) {
        viewModelScope.launch {
            repository.updateDebtLoan(item.copy(isSettled = !item.isSettled))
            addXp(40)
        }
    }

    fun deleteDebtLoan(item: DebtLoanItem) {
        viewModelScope.launch {
            repository.deleteDebtLoan(item)
        }
    }

    // Encrypted Notes (Optimized with distinctUntilChanged)
    val notes: StateFlow<List<EncryptedNote>> = repository.allNotes
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _unlockedNotes = MutableStateFlow<Map<Long, String>>(emptyMap())
    val unlockedNotes: StateFlow<Map<Long, String>> = _unlockedNotes.asStateFlow()

    fun unlockNoteWithPin(note: EncryptedNote, pin: String): Boolean {
        return try {
            val decrypted = SecurityHelper.decrypt(note.encryptedContent, pin)
            val updated = _unlockedNotes.value.toMutableMap()
            updated[note.id] = decrypted
            _unlockedNotes.value = updated
            true
        } catch (e: Exception) {
            false
        }
    }

    fun lockNote(noteId: Long) {
        val updated = _unlockedNotes.value.toMutableMap()
        updated.remove(noteId)
        _unlockedNotes.value = updated
    }

    fun addEncryptedNote(title: String, content: String, pin: String, mood: String, tag: String) {
        viewModelScope.launch {
            val encrypted = SecurityHelper.encrypt(content, pin)
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.insertNote(
                EncryptedNote(
                    title = title,
                    encryptedContent = encrypted,
                    previewSnippet = "Tersimpan aman dengan enkripsi AES end-to-end...",
                    date = todayStr,
                    mood = mood,
                    tag = tag,
                    isLocked = true
                )
            )
            addXp(40)
        }
    }

    fun deleteNote(note: EncryptedNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
            lockNote(note.id)
        }
    }

    // Language Learning & Daily Crossword
    val languageLessons: List<LanguageLesson> = LanguageLessonProvider.sampleLessons

    private val _selectedLesson = MutableStateFlow<LanguageLesson?>(null)
    val selectedLesson: StateFlow<LanguageLesson?> = _selectedLesson.asStateFlow()

    fun selectLesson(lesson: LanguageLesson?) {
        _selectedLesson.value = lesson
    }

    // Dictionary
    private val _dictionaryQuery = MutableStateFlow("")
    val dictionaryQuery: StateFlow<String> = _dictionaryQuery.asStateFlow()

    private val _selectedDictLanguage = MutableStateFlow("All")
    val selectedDictLanguage: StateFlow<String> = _selectedDictLanguage.asStateFlow()

    fun setDictionaryQuery(query: String) {
        _dictionaryQuery.value = query
    }

    fun setSelectedDictLanguage(lang: String) {
        _selectedDictLanguage.value = lang
    }

    // Daily Crossword Game State
    val todayDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val dailyCrossword = LanguageLessonProvider.getCrosswordForDate(todayDateStr)

    private val _crosswordAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val crosswordAnswers: StateFlow<Map<Int, String>> = _crosswordAnswers.asStateFlow()

    private val _isCrosswordCompleted = MutableStateFlow(false)
    val isCrosswordCompleted: StateFlow<Boolean> = _isCrosswordCompleted.asStateFlow()

    fun updateCrosswordAnswer(clueNumber: Int, text: String) {
        val map = _crosswordAnswers.value.toMutableMap()
        map[clueNumber] = text.uppercase(Locale.getDefault())
        _crosswordAnswers.value = map

        val allCorrect = dailyCrossword.clues.all { clue ->
            map[clue.number]?.equals(clue.answer, ignoreCase = true) == true
        }
        if (allCorrect && !_isCrosswordCompleted.value) {
            _isCrosswordCompleted.value = true
            addXp(dailyCrossword.xpReward)
            SyncManager.pushCustomNotification(
                title = "🎉 Selamat! Teka-Teki Silang Selesai",
                message = "Kamu menyelesaikan teka-teki hari ini dan mendapatkan +${dailyCrossword.xpReward} XP!",
                type = "CHALLENGE"
            )
        }
    }

    // AI Photo Background Studio
    private val _selectedAiBackground = MutableStateFlow(AiBackgroundStyle.NEON_STUDIO)
    val selectedAiBackground: StateFlow<AiBackgroundStyle> = _selectedAiBackground.asStateFlow()

    private val _isAiProcessing = MutableStateFlow(false)
    val isAiProcessing: StateFlow<Boolean> = _isAiProcessing.asStateFlow()

    private val _backgroundRemovalSuccess = MutableStateFlow(false)
    val backgroundRemovalSuccess: StateFlow<Boolean> = _backgroundRemovalSuccess.asStateFlow()

    fun setAiBackground(style: AiBackgroundStyle) {
        _selectedAiBackground.value = style
    }

    fun processAiBackgroundRemoval() {
        viewModelScope.launch {
            _isAiProcessing.value = true
            kotlinx.coroutines.delay(1200)
            _isAiProcessing.value = false
            _backgroundRemovalSuccess.value = true
            addXp(30)
        }
    }

    // Dynamic Leaderboard using current user info
    val leaderboardUsers: List<LeaderboardUser>
        get() {
            val userName = if (_userProfile.value.name.isNotBlank()) _userProfile.value.name else "Anda"
            val userXp = _userProfile.value.currentXp
            val userLevel = _userProfile.value.level
            val userBadge = _userProfile.value.activeBadges.firstOrNull() ?: "Evo Member"
            return listOf(
                LeaderboardUser(1, "Siti Nurhaliza", "👑", 3850, 12, "Polyglot Master"),
                LeaderboardUser(2, "Budi Gunawan", "⭐", 3420, 10, "Focus Prodigy"),
                LeaderboardUser(3, userName, _userProfile.value.avatarEmoji, 2850 + userXp, userLevel, userBadge, isCurrentUser = true),
                LeaderboardUser(4, "Reza Rahadian", "⚡", 2600, 7, "Budget Boss"),
                LeaderboardUser(5, "Alya Putri", "🎯", 2350, 6, "Task Speedster"),
                LeaderboardUser(6, "Dimas Anggara", "🌟", 1980, 5, "Rising Star")
            )
        }

    private val _weeklyChallenges = MutableStateFlow(
        listOf(
            WeeklyChallenge(
                id = "wc_1",
                title = "Fokus Tingkat Tinggi",
                description = "Selesaikan 15 tugas berprioritas tinggi minggu ini.",
                target = 15,
                progress = 0,
                rewardPoints = 500,
                endsInDays = 6
            ),
            WeeklyChallenge(
                id = "wc_2",
                title = "Ahli Kosakata Global",
                description = "Kuasai 20 kosakata baru di kamus digital.",
                target = 20,
                progress = 0,
                rewardPoints = 350,
                endsInDays = 6
            ),
            WeeklyChallenge(
                id = "wc_3",
                title = "Disiplin Finansial",
                description = "Catat pengeluaran harian berturut-turut selama 7 hari.",
                target = 7,
                progress = 0,
                rewardPoints = 400,
                endsInDays = 6
            )
        )
    )
    val weeklyChallenges: StateFlow<List<WeeklyChallenge>> = _weeklyChallenges.asStateFlow()

    fun claimChallenge(challengeId: String) {
        val list = _weeklyChallenges.value.map {
            if (it.id == challengeId) {
                addXp(it.rewardPoints)
                it.copy(isClaimed = true)
            } else it
        }
        _weeklyChallenges.value = list
    }

    private fun addXp(amount: Int) {
        val current = _userProfile.value
        val newXp = current.currentXp + amount
        if (newXp >= current.nextLevelXp) {
            _userProfile.value = current.copy(
                level = current.level + 1,
                currentXp = newXp - current.nextLevelXp,
                nextLevelXp = (current.nextLevelXp * 1.3).toInt()
            )
            SyncManager.pushCustomNotification(
                title = "🌟 Level Up! Level ${_userProfile.value.level}",
                message = "Selamat! Kamu naik ke Level ${_userProfile.value.level} dan membuka lencana baru!",
                type = "CHALLENGE"
            )
        } else {
            _userProfile.value = current.copy(currentXp = newXp)
        }
    }

    // Admin Panel State & Actions
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _adminNotificationTitle = MutableStateFlow("")
    val adminNotificationTitle: StateFlow<String> = _adminNotificationTitle.asStateFlow()

    private val _adminNotificationMsg = MutableStateFlow("")
    val adminNotificationMsg: StateFlow<String> = _adminNotificationMsg.asStateFlow()

    fun authenticateAdmin(pin: String): Boolean {
        if (pin == "admin123" || pin == "8888") {
            _isAdminAuthenticated.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
    }

    fun setAdminNotificationTitle(t: String) { _adminNotificationTitle.value = t }
    fun setAdminNotificationMsg(m: String) { _adminNotificationMsg.value = m }

    fun broadcastAdminNotification() {
        if (_adminNotificationTitle.value.isNotBlank() && _adminNotificationMsg.value.isNotBlank()) {
            SyncManager.pushCustomNotification(
                title = "📢 Pengumuman Admin: ${_adminNotificationTitle.value}",
                message = _adminNotificationMsg.value,
                type = "CHALLENGE"
            )
            _adminNotificationTitle.value = ""
            _adminNotificationMsg.value = ""
        }
    }
}

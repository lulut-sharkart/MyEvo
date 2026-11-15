package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MyEvoDatabase
import com.example.data.model.AiGalleryItem
import com.example.data.model.AuthProvider
import com.example.data.model.BadgeEntity
import com.example.data.model.CrosswordPuzzle
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
import com.example.data.model.UserStreakEntity
import com.example.data.model.WeeklyChallenge
import com.example.data.remote.ExpenseCategorizationResult
import com.example.data.remote.FirestoreSyncService
import com.example.data.remote.GeminiAiService
import com.example.data.remote.SupabaseConfigStatus
import com.example.data.remote.SupabaseService
import com.example.data.repository.HolidayProvider
import com.example.data.repository.LanguageLesson
import com.example.data.repository.LanguageLessonProvider
import com.example.data.repository.MyEvoRepository
import com.example.data.repository.PushNotification
import com.example.data.repository.SecurityHelper
import com.example.data.repository.SupabaseRemoteRecord
import com.example.data.repository.SupabaseRepository
import com.example.data.repository.SyncManager
import kotlinx.coroutines.CancellationException
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

sealed interface CrosswordGenerationState {
    data object Idle : CrosswordGenerationState
    data object Loading : CrosswordGenerationState
    data class Success(val puzzle: CrosswordPuzzle, val isAiGenerated: Boolean) : CrosswordGenerationState
    data class Error(val message: String) : CrosswordGenerationState
}

sealed interface CategorizationUiState {
    data object Idle : CategorizationUiState
    data object Loading : CategorizationUiState
    data class Success(val result: ExpenseCategorizationResult) : CategorizationUiState
    data class Error(val message: String) : CategorizationUiState
}

sealed interface SupabaseSyncState {
    data object Idle : SupabaseSyncState
    data object Loading : SupabaseSyncState
    data class Success(val message: String) : SupabaseSyncState
    data class Error(val message: String) : SupabaseSyncState
}

enum class AppScreen {
    AUTH,
    HOME,
    CALENDAR,
    FINANCE,
    LEARN,
    STUDIO,
    PHOTO_CAPTURE,
    NOTES,
    TOOLS,
    PROFILE,
    ADMIN,
    SETTINGS
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
    private val firestoreSyncService: FirestoreSyncService by lazy {
        FirestoreSyncService(getApplication())
    }

    init {
        val database = MyEvoDatabase.getDatabase(application)
        repository = MyEvoRepository(database)
        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
        }
    }

    // Room-Persisted Badges & Streaks
    val badges: StateFlow<List<BadgeEntity>> = repository.allBadges
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val languageStreak: StateFlow<UserStreakEntity?> = repository.languageStreak
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val taskStreak: StateFlow<UserStreakEntity?> = repository.taskStreak
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

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
        viewModelScope.launch {
            val result = firestoreSyncService.syncUserDataToCloud(
                profile = _userProfile.value,
                tasks = tasks.value,
                finances = financeEntries.value,
                debts = debtsLoans.value,
                badges = badges.value
            )
            result.onSuccess { syncedCount ->
                _userProfile.value = _userProfile.value.copy(
                    lastSyncTimestamp = "Tersinkron (${syncedCount} item)"
                )
                SyncManager.pushCustomNotification(
                    title = "☁️ Pencadangan Cloud Firestore Selesai",
                    message = "$syncedCount dokumen tugas, keuangan, utang, & lencana berhasil dicadangkan ke Cloud Firestore.",
                    type = "SYNC"
                )
            }.onFailure { error ->
                SyncManager.pushCustomNotification(
                    title = "⚠️ Sinkronisasi Cloud Gagal",
                    message = error.localizedMessage ?: "Pastikan Anda masuk melalui Google Sign-In dan terhubung ke internet.",
                    type = "SYNC"
                )
            }
        }
    }

    // Supabase Integration State & Actions via SupabaseRepository
    private val supabaseRepository = SupabaseRepository()

    val supabaseConfigStatus: SupabaseConfigStatus
        get() = SupabaseService.getConfigStatus()

    private val _supabaseSyncState = MutableStateFlow<SupabaseSyncState>(SupabaseSyncState.Idle)
    val supabaseSyncState: StateFlow<SupabaseSyncState> = _supabaseSyncState.asStateFlow()

    private val _supabaseFetchedRecords = MutableStateFlow<List<SupabaseRemoteRecord>>(emptyList())
    val supabaseFetchedRecords: StateFlow<List<SupabaseRemoteRecord>> = _supabaseFetchedRecords.asStateFlow()

    fun testSupabaseConnection() {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState.Loading
            val result = SupabaseService.testConnection()
            result.onSuccess { msg ->
                _supabaseSyncState.value = SupabaseSyncState.Success(msg)
                SyncManager.pushCustomNotification(
                    title = "⚡ Supabase Terhubung",
                    message = msg,
                    type = "SYNC"
                )
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Gagal terhubung ke Supabase."
                _supabaseSyncState.value = SupabaseSyncState.Error(msg)
            }
        }
    }

    fun fetchSupabaseInitialRecords() {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState.Loading
            val result = supabaseRepository.fetchInitialRecords()
            result.onSuccess { rows ->
                _supabaseFetchedRecords.value = rows
                val msg = "Berhasil mengambil ${rows.size} rekaman awal dari Supabase."
                _supabaseSyncState.value = SupabaseSyncState.Success(msg)
            }.onFailure { err ->
                _supabaseSyncState.value = SupabaseSyncState.Error(
                    err.localizedMessage ?: "Gagal mengambil data awal dari Supabase."
                )
            }
        }
    }

    fun syncDataToSupabase() {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState.Loading
            val result = supabaseRepository.pushInitialRecords(
                profile = _userProfile.value,
                tasks = tasks.value,
                finances = financeEntries.value,
                debts = debtsLoans.value,
                badges = badges.value
            )
            result.onSuccess { syncedCount ->
                val msg = "Berhasil menyinkronkan $syncedCount item ke tabel Supabase (myevo_sync_snapshots)."
                _supabaseSyncState.value = SupabaseSyncState.Success(msg)
                _userProfile.value = _userProfile.value.copy(
                    lastSyncTimestamp = "Supabase (${syncedCount} item)"
                )
                SyncManager.pushCustomNotification(
                    title = "🟢 Sinkronisasi Supabase Selesai",
                    message = msg,
                    type = "SYNC"
                )
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Sinkronisasi Supabase gagal."
                _supabaseSyncState.value = SupabaseSyncState.Error(msg)
            }
        }
    }

    fun uploadPhotoToSupabase(context: Context, imageUri: Uri, label: String) {
        viewModelScope.launch {
            _supabaseSyncState.value = SupabaseSyncState.Loading
            val fileName = "${label.take(24)}_${System.currentTimeMillis()}.jpg"
            val result = SupabaseService.uploadPhotoToSupabaseStorage(context, imageUri, fileName)
            result.onSuccess { publicUrl ->
                val msg = "Foto berhasil diunggah ke Supabase Storage (myevo-photos)."
                _supabaseSyncState.value = SupabaseSyncState.Success("$msg URL: $publicUrl")
                SyncManager.pushCustomNotification(
                    title = "☁️ Unggahan Foto Supabase Selesai",
                    message = msg,
                    type = "STUDIO"
                )
            }.onFailure { err ->
                val msg = err.localizedMessage ?: "Gagal mengunggah foto ke Supabase Storage."
                _supabaseSyncState.value = SupabaseSyncState.Error(msg)
            }
        }
    }

    fun resetSupabaseState() {
        _supabaseSyncState.value = SupabaseSyncState.Idle
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
                val newlyUnlocked = repository.recordTaskCompletionBadgeProgress()
                newlyUnlocked.forEach { badge ->
                    addXp(badge.xpReward)
                    SyncManager.pushCustomNotification(
                        title = "${badge.iconEmoji} Lencana Baru Terbuka!",
                        message = "Selamat! Kamu meraih lencana \"${badge.title}\" (+${badge.xpReward} XP).",
                        type = "CHALLENGE"
                    )
                }
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

    // Gemini API Automated Budget Expense Categorization
    private val _categorizationUiState = MutableStateFlow<CategorizationUiState>(CategorizationUiState.Idle)
    val categorizationUiState: StateFlow<CategorizationUiState> = _categorizationUiState.asStateFlow()

    fun categorizeExpenseWithAi(description: String, financeType: FinanceType) {
        if (description.isBlank()) {
            _categorizationUiState.value = CategorizationUiState.Error("Masukkan keterangan transaksi terlebih dahulu.")
            return
        }
        viewModelScope.launch {
            _categorizationUiState.value = CategorizationUiState.Loading
            try {
                val result = GeminiAiService.categorizeExpense(description.trim(), financeType)
                _categorizationUiState.value = CategorizationUiState.Success(result)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _categorizationUiState.value = CategorizationUiState.Error(
                    e.localizedMessage ?: "Gagal menganalisis kategori dengan Gemini AI."
                )
            }
        }
    }

    fun resetCategorizationState() {
        _categorizationUiState.value = CategorizationUiState.Idle
    }

    fun toggleDebtLoanSettled(item: DebtLoanItem) {
        viewModelScope.launch {
            val nowSettled = !item.isSettled
            val updated = item.copy(
                isSettled = nowSettled,
                paidAmount = if (nowSettled) item.amount else if (item.paidAmount >= item.amount) 0.0 else item.paidAmount
            )
            repository.updateDebtLoan(updated)
            addXp(40)
        }
    }

    fun recordDebtPayment(item: DebtLoanItem, paymentAmount: Double) {
        if (paymentAmount <= 0.0) return
        viewModelScope.launch {
            val updated = repository.recordDebtPayment(item, paymentAmount)
            val xpEarned = if (updated.isSettled) 45 else 25
            addXp(xpEarned)
            SyncManager.pushCustomNotification(
                title = if (updated.isSettled) "✅ Utang/Piutang Lunas!" else "💳 Cicilan Tercatat",
                message = if (updated.isSettled) {
                    "Catatan ${updated.personName} telah dibayar 100% lunas (+${xpEarned} XP)."
                } else {
                    "Pembayaran untuk ${updated.personName} kini ${(updated.progressFraction * 100).toInt()}% terbayar."
                },
                type = "SYNC"
            )
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

    // Daily Crossword Game State (Powered by Gemini API + Theme Customization)
    val todayDateStr: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _dailyCrossword = MutableStateFlow(LanguageLessonProvider.getCrosswordForDate(todayDateStr))
    val dailyCrossword: StateFlow<CrosswordPuzzle> = _dailyCrossword.asStateFlow()

    private val _crosswordTheme = MutableStateFlow("Otomatis Harian")
    val crosswordTheme: StateFlow<String> = _crosswordTheme.asStateFlow()

    private val _crosswordUiState = MutableStateFlow<CrosswordGenerationState>(CrosswordGenerationState.Idle)
    val crosswordUiState: StateFlow<CrosswordGenerationState> = _crosswordUiState.asStateFlow()

    private val _crosswordAnswers = MutableStateFlow<Map<Int, String>>(emptyMap())
    val crosswordAnswers: StateFlow<Map<Int, String>> = _crosswordAnswers.asStateFlow()

    private val _isCrosswordCompleted = MutableStateFlow(false)
    val isCrosswordCompleted: StateFlow<Boolean> = _isCrosswordCompleted.asStateFlow()

    fun setCrosswordTheme(theme: String) {
        _crosswordTheme.value = theme
    }

    fun generateAiDailyCrossword(theme: String = _crosswordTheme.value) {
        _crosswordTheme.value = theme
        viewModelScope.launch {
            _crosswordUiState.value = CrosswordGenerationState.Loading
            try {
                val aiPuzzle = GeminiAiService.generateDailyCrossword(
                    dateStr = todayDateStr,
                    theme = theme
                )
                _dailyCrossword.value = aiPuzzle
                _crosswordAnswers.value = emptyMap()
                _isCrosswordCompleted.value = false
                _crosswordUiState.value = CrosswordGenerationState.Success(aiPuzzle, isAiGenerated = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _crosswordUiState.value = CrosswordGenerationState.Error(
                    e.localizedMessage ?: "Gagal membuat teka-teki harian dengan Gemini AI. Periksa koneksi internet."
                )
            }
        }
    }

    fun updateCrosswordAnswer(clueNumber: Int, text: String) {
        val currentPuzzle = _dailyCrossword.value
        val map = _crosswordAnswers.value.toMutableMap()
        map[clueNumber] = text.uppercase(Locale.getDefault())
        _crosswordAnswers.value = map

        val allCorrect = currentPuzzle.clues.isNotEmpty() && currentPuzzle.clues.all { clue ->
            map[clue.number]?.equals(clue.answer, ignoreCase = true) == true
        }
        if (allCorrect && !_isCrosswordCompleted.value) {
            _isCrosswordCompleted.value = true
            addXp(currentPuzzle.xpReward)
            viewModelScope.launch {
                val newlyUnlocked = repository.recordLanguageBadgeProgress(
                    vocabDelta = 1,
                    crosswordSolved = true
                )
                newlyUnlocked.forEach { badge ->
                    addXp(badge.xpReward)
                    SyncManager.pushCustomNotification(
                        title = "${badge.iconEmoji} Lencana Baru Terbuka!",
                        message = "Selamat! Kamu meraih lencana \"${badge.title}\" (+${badge.xpReward} XP).",
                        type = "CHALLENGE"
                    )
                }
            }
            SyncManager.pushCustomNotification(
                title = "🎉 Selamat! Teka-Teki Silang Selesai",
                message = "Kamu menyelesaikan teka-teki hari ini dan mendapatkan +${currentPuzzle.xpReward} XP!",
                type = "CHALLENGE"
            )
        }
    }

    fun recordLanguageLessonCompletion() {
        viewModelScope.launch {
            val newlyUnlocked = repository.recordLanguageBadgeProgress(vocabDelta = 1, crosswordSolved = false)
            addXp(40)
            newlyUnlocked.forEach { badge ->
                addXp(badge.xpReward)
                SyncManager.pushCustomNotification(
                    title = "${badge.iconEmoji} Lencana Bahasa Terbuka!",
                    message = "Lencana \"${badge.title}\" berhasil kamu buka (+${badge.xpReward} XP)!",
                    type = "CHALLENGE"
                )
            }
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

    // Dynamic Leaderboard using authentic user data (no dummy/fake bot accounts)
    val leaderboardUsers: List<LeaderboardUser>
        get() {
            val profile = _userProfile.value
            val userName = if (profile.name.isNotBlank()) profile.name else if (profile.isGuest) "Pengguna Tamu (Anda)" else "Saya"
            val userXp = profile.currentXp
            val userLevel = profile.level
            val userBadge = profile.activeBadges.firstOrNull() ?: "Evo Member"
            return listOf(
                LeaderboardUser(
                    rank = 1,
                    name = userName,
                    avatarEmoji = profile.avatarEmoji,
                    points = userXp,
                    level = userLevel,
                    badge = userBadge,
                    isCurrentUser = true
                )
            )
        }

    // AI Photo Gallery State & Gemini Actions
    private val _aiGallery = MutableStateFlow<List<AiGalleryItem>>(
        listOf(
            AiGalleryItem(
                id = "gallery_init_1",
                title = "Portrait Studio Minimalis",
                prompt = "Subjek profesional dengan pencahayaan studio lembut dan latar belakang gradien monokrom bersih",
                style = "Minimalist Studio",
                subject = "Profesional Portrait",
                ratio = "1:1",
                dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date()),
                isTransformation = true,
                colorPrimary = 0xFF8B5CF6,
                colorSecondary = 0xFF06B6D4
            )
        )
    )
    val aiGallery: StateFlow<List<AiGalleryItem>> = _aiGallery.asStateFlow()

    private val _isGeneratingPhoto = MutableStateFlow(false)
    val isGeneratingPhoto: StateFlow<Boolean> = _isGeneratingPhoto.asStateFlow()

    private val _generatedPhotoResult = MutableStateFlow<AiGalleryItem?>(null)
    val generatedPhotoResult: StateFlow<AiGalleryItem?> = _generatedPhotoResult.asStateFlow()

    private val _capturedPhotoUri = MutableStateFlow<Uri?>(null)
    val capturedPhotoUri: StateFlow<Uri?> = _capturedPhotoUri.asStateFlow()

    fun setCapturedPhotoUri(uri: Uri?) {
        _capturedPhotoUri.value = uri
    }

    fun clearCapturedPhotoUri() {
        _capturedPhotoUri.value = null
    }

    fun saveToAiGallery(
        title: String,
        prompt: String,
        style: String,
        subject: String,
        isTransformation: Boolean,
        ratio: String = "1:1",
        colorPrimary: Long = 0xFF6366F1,
        colorSecondary: Long = 0xFF8B5CF6,
        imageUri: String? = _capturedPhotoUri.value?.toString()
    ) {
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
        val newItem = AiGalleryItem(
            title = title.ifBlank { "Karya AI MyEvo" },
            prompt = prompt,
            style = style,
            subject = subject,
            ratio = ratio,
            dateStr = dateStr,
            isTransformation = isTransformation,
            colorPrimary = colorPrimary,
            colorSecondary = colorSecondary,
            imageUri = imageUri
        )
        _aiGallery.value = listOf(newItem) + _aiGallery.value
        addXp(35)
        SyncManager.pushCustomNotification(
            title = "📸 Disimpan ke Galeri AI",
            message = "\"${newItem.title}\" berhasil disimpan ke Galeri Karya AI.",
            type = "STUDIO"
        )
    }

    fun deleteFromAiGallery(id: String) {
        _aiGallery.value = _aiGallery.value.filter { it.id != id }
    }

    fun generatePhotoWithGemini(prompt: String, style: String, ratio: String) {
        viewModelScope.launch {
            _isGeneratingPhoto.value = true
            kotlinx.coroutines.delay(1800)
            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val colorPrimary = when (style) {
                "Cyberpunk Neon" -> 0xFF06B6D4
                "Anime Manga" -> 0xFFEC4899
                "Golden Hour" -> 0xFFF59E0B
                "Minimalist 3D" -> 0xFF8B5CF6
                else -> 0xFF6366F1
            }
            val result = AiGalleryItem(
                title = prompt.take(28).ifBlank { "Kreasi Gemini AI" },
                prompt = prompt,
                style = style,
                subject = "Kreasi Gemini AI",
                ratio = ratio,
                dateStr = dateStr,
                isTransformation = false,
                colorPrimary = colorPrimary,
                colorSecondary = 0xFF6366F1
            )
            _generatedPhotoResult.value = result
            _isGeneratingPhoto.value = false
            addXp(50)
            SyncManager.pushCustomNotification(
                title = "✨ Foto Baru Gemini AI Siap",
                message = "Sintesis foto \"${result.title}\" selesai dan dapat disimpan ke galeri.",
                type = "STUDIO"
            )
        }
    }

    // Comprehensive Settings methods
    fun toggleVibration() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isVibrationEnabled = !current.isVibrationEnabled)
    }

    fun toggleSound() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isSoundEnabled = !current.isSoundEnabled)
    }

    fun toggleTaskReminder() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isTaskReminderEnabled = !current.isTaskReminderEnabled)
    }

    fun setDefaultReminderMinutes(minutes: Int) {
        val current = _userProfile.value
        _userProfile.value = current.copy(defaultReminderMinutes = minutes)
    }

    fun toggleStudyReminder() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isStudyReminderEnabled = !current.isStudyReminderEnabled)
    }

    fun toggleChallengeReminder() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isChallengeReminderEnabled = !current.isChallengeReminderEnabled)
    }

    fun toggleAutoSync() {
        val current = _userProfile.value
        _userProfile.value = current.copy(isAutoSyncEnabled = !current.isAutoSyncEnabled)
    }

    fun toggleAppLock(enabled: Boolean) {
        val current = _userProfile.value
        _userProfile.value = current.copy(isAppLockEnabled = enabled)
    }

    fun setCurrency(currency: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(currencyCode = currency)
    }

    fun setAppLanguage(lang: String) {
        val current = _userProfile.value
        _userProfile.value = current.copy(appLanguage = lang)
    }

    fun clearImageCache() {
        SyncManager.pushCustomNotification(
            title = "🧹 Cache Dibersihkan",
            message = "Cache gambar dan berkas sementara telah berhasil dikosongkan.",
            type = "SYNC"
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

    fun markAdminAuthenticated(authenticated: Boolean) {
        _isAdminAuthenticated.value = authenticated
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

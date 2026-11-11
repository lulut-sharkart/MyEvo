package com.example.data.repository

import com.example.data.local.MyEvoDatabase
import com.example.data.model.DebtLoanItem
import com.example.data.model.DictionaryWord
import com.example.data.model.EncryptedNote
import com.example.data.model.FinanceItem
import com.example.data.model.ScheduleEvent
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow

class MyEvoRepository(private val database: MyEvoDatabase) {
    private val taskDao = database.taskDao()
    private val scheduleDao = database.scheduleDao()
    private val financeDao = database.financeDao()
    private val noteDao = database.noteDao()
    private val dictionaryDao = database.dictionaryDao()

    // Tasks abstraction via TaskRepository
    val taskRepository = TaskRepository(taskDao)
    val allTasks: Flow<List<TaskItem>> = taskRepository.allTasks

    suspend fun insertTask(task: TaskItem): Long = taskRepository.insertTask(task)
    suspend fun updateTask(task: TaskItem) = taskRepository.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskRepository.deleteTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskRepository.setTaskCompleted(id, completed)
    suspend fun clearAllTasks() = taskRepository.clearAllTasks()

    // Schedule & Holidays
    val allEvents: Flow<List<ScheduleEvent>> = scheduleDao.getAllEvents()

    fun getEventsByDate(date: String): Flow<List<ScheduleEvent>> = scheduleDao.getEventsByDate(date)

    suspend fun insertEvent(event: ScheduleEvent) = scheduleDao.insertEvent(event)
    suspend fun deleteEvent(event: ScheduleEvent) = scheduleDao.deleteEvent(event)

    // Holiday Calendar Provider with API & Local Room Sync
    val holidayCalendarProvider = HolidayCalendarProvider(scheduleDao)

    suspend fun syncHolidaysForCountry(countryCode: String, year: Int = 2026) {
        holidayCalendarProvider.fetchAndSyncHolidays(countryCode, year)
    }

    // Finance & Debts
    val allFinance: Flow<List<FinanceItem>> = financeDao.getAllFinanceEntries()
    val allDebtsLoans: Flow<List<DebtLoanItem>> = financeDao.getAllDebtsLoans()

    suspend fun insertFinance(finance: FinanceItem) = financeDao.insertFinance(finance)
    suspend fun deleteFinance(finance: FinanceItem) = financeDao.deleteFinance(finance)

    suspend fun insertDebtLoan(debtLoan: DebtLoanItem) = financeDao.insertDebtLoan(debtLoan)
    suspend fun updateDebtLoan(debtLoan: DebtLoanItem) = financeDao.updateDebtLoan(debtLoan)
    suspend fun deleteDebtLoan(debtLoan: DebtLoanItem) = financeDao.deleteDebtLoan(debtLoan)

    // Encrypted Notes
    val allNotes: Flow<List<EncryptedNote>> = noteDao.getAllNotes()

    suspend fun insertNote(note: EncryptedNote) = noteDao.insertNote(note)
    suspend fun updateNote(note: EncryptedNote) = noteDao.updateNote(note)
    suspend fun deleteNote(note: EncryptedNote) = noteDao.deleteNote(note)

    // Dictionary
    fun getWordsByLanguage(language: String): Flow<List<DictionaryWord>> = dictionaryDao.getWordsByLanguage(language)
    fun searchWords(query: String): Flow<List<DictionaryWord>> = dictionaryDao.searchWords(query)

    // Clear all dummy data
    suspend fun clearAllUserData() {
        taskDao.clearAllTasks()
        financeDao.clearAllFinance()
        financeDao.clearAllDebtsLoans()
        noteDao.clearAllNotes()
        scheduleDao.clearUserEvents()
    }

    suspend fun populateInitialDataIfEmpty() {
        val count = dictionaryDao.getWordCount()
        if (count == 0) {
            dictionaryDao.insertWords(LanguageLessonProvider.initialDictionaryWords)
            // Initialize official national holidays for default country
            syncHolidaysForCountry("ID", 2026)
        }
    }
}

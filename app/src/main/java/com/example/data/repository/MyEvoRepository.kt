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

    // Tasks
    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskItem): Long {
        val id = taskDao.insertTask(task)
        if (task.priority == TaskPriority.HIGH) {
            SyncManager.pushCustomNotification(
                title = "⚡ Pengingat Prioritas: ${task.title}",
                message = "Tenggat waktu ${task.dueDate} pukul ${task.dueTime}.",
                type = "REMINDER"
            )
        }
        return id
    }

    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskDao.setTaskCompleted(id, completed)

    // Schedule & Holidays
    val allEvents: Flow<List<ScheduleEvent>> = scheduleDao.getAllEvents()

    fun getEventsByDate(date: String): Flow<List<ScheduleEvent>> = scheduleDao.getEventsByDate(date)

    suspend fun insertEvent(event: ScheduleEvent) = scheduleDao.insertEvent(event)
    suspend fun deleteEvent(event: ScheduleEvent) = scheduleDao.deleteEvent(event)

    suspend fun syncHolidaysForCountry(countryCode: String, year: Int = 2026) {
        val holidays = HolidayProvider.getHolidaysForCountry(countryCode, year)
        scheduleDao.insertEvents(holidays)
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

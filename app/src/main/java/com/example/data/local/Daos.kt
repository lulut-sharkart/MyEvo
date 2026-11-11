package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DebtLoanItem
import com.example.data.model.DictionaryWord
import com.example.data.model.EncryptedNote
import com.example.data.model.FinanceItem
import com.example.data.model.LanguageLearningEntity
import com.example.data.model.ScheduleEvent
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority ASC, dueDate ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun setTaskCompleted(id: Long, completed: Boolean)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_events ORDER BY date ASC, startTime ASC")
    fun getAllEvents(): Flow<List<ScheduleEvent>>

    @Query("SELECT * FROM schedule_events WHERE date = :date ORDER BY startTime ASC")
    fun getEventsByDate(date: String): Flow<List<ScheduleEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ScheduleEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ScheduleEvent>)

    @Delete
    suspend fun deleteEvent(event: ScheduleEvent)

    @Query("DELETE FROM schedule_events WHERE isHoliday = 1 AND countryCode = :countryCode")
    suspend fun deleteHolidaysByCountry(countryCode: String)

    @Query("DELETE FROM schedule_events WHERE isHoliday = 0")
    suspend fun clearUserEvents()
}

@Dao
interface FinanceDao {
    @Query("SELECT * FROM finance_entries ORDER BY date DESC, id DESC")
    fun getAllFinanceEntries(): Flow<List<FinanceItem>>

    @Query("SELECT * FROM finance_entries WHERE monthYear = :monthYear ORDER BY date DESC")
    fun getFinanceByMonth(monthYear: String): Flow<List<FinanceItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFinance(item: FinanceItem): Long

    @Delete
    suspend fun deleteFinance(item: FinanceItem)

    @Query("DELETE FROM finance_entries")
    suspend fun clearAllFinance()

    @Query("SELECT * FROM debts_loans ORDER BY isSettled ASC, dueDate ASC")
    fun getAllDebtsLoans(): Flow<List<DebtLoanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtLoan(item: DebtLoanItem): Long

    @Update
    suspend fun updateDebtLoan(item: DebtLoanItem)

    @Delete
    suspend fun deleteDebtLoan(item: DebtLoanItem)

    @Query("DELETE FROM debts_loans")
    suspend fun clearAllDebtsLoans()
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM encrypted_notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<EncryptedNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: EncryptedNote): Long

    @Update
    suspend fun updateNote(note: EncryptedNote)

    @Delete
    suspend fun deleteNote(note: EncryptedNote)

    @Query("DELETE FROM encrypted_notes")
    suspend fun clearAllNotes()
}

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM dictionary_words WHERE language = :lang ORDER BY word ASC")
    fun getWordsByLanguage(lang: String): Flow<List<DictionaryWord>>

    @Query("SELECT * FROM dictionary_words WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchWords(query: String): Flow<List<DictionaryWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<DictionaryWord>)

    @Query("SELECT COUNT(*) FROM dictionary_words")
    suspend fun getWordCount(): Int
}

@Dao
interface LanguageLearningDao {
    @Query("SELECT * FROM language_learning ORDER BY isMastered ASC, masteryLevel ASC, id DESC")
    fun getAllVocabulary(): Flow<List<LanguageLearningEntity>>

    @Query("SELECT * FROM language_learning WHERE language = :lang ORDER BY isMastered ASC, masteryLevel ASC")
    fun getVocabularyByLanguage(lang: String): Flow<List<LanguageLearningEntity>>

    @Query("SELECT * FROM language_learning WHERE word LIKE '%' || :query || '%' OR translation LIKE '%' || :query || '%'")
    fun searchVocabulary(query: String): Flow<List<LanguageLearningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: LanguageLearningEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<LanguageLearningEntity>)

    @Update
    suspend fun updateWord(word: LanguageLearningEntity)

    @Delete
    suspend fun deleteWord(word: LanguageLearningEntity)

    @Query("UPDATE language_learning SET masteryLevel = :level, isMastered = :mastered, reviewCount = :reviews, lastReviewedAt = :timestamp WHERE id = :id")
    suspend fun updateMastery(id: Long, level: Int, mastered: Boolean, reviews: Int, timestamp: Long)

    @Query("SELECT COUNT(*) FROM language_learning")
    suspend fun getWordCount(): Int

    @Query("DELETE FROM language_learning")
    suspend fun clearAll()
}

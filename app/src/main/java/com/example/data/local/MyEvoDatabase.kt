package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.DebtLoanItem
import com.example.data.model.DictionaryWord
import com.example.data.model.EncryptedNote
import com.example.data.model.FinanceItem
import com.example.data.model.ScheduleEvent
import com.example.data.model.TaskItem

@Database(
    entities = [
        TaskItem::class,
        ScheduleEvent::class,
        FinanceItem::class,
        DebtLoanItem::class,
        EncryptedNote::class,
        DictionaryWord::class,
        com.example.data.model.LanguageLearningEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MyEvoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun financeDao(): FinanceDao
    abstract fun noteDao(): NoteDao
    abstract fun dictionaryDao(): DictionaryDao
    abstract fun languageLearningDao(): LanguageLearningDao

    companion object {
        @Volatile
        private var INSTANCE: MyEvoDatabase? = null

        fun getDatabase(context: Context): MyEvoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyEvoDatabase::class.java,
                    "myevo_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

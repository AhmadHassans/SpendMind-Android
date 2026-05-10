package com.spendmindai.app.core.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.spendmindai.app.core.data.database.dao.CategoryDao
import com.spendmindai.app.core.data.database.dao.ExpenseDao
import com.spendmindai.app.core.data.database.dao.RecurringExpenseDao
import com.spendmindai.app.core.data.database.dao.UserPreferencesDao
import com.spendmindai.app.core.data.database.entities.CategoryEntity
import com.spendmindai.app.core.data.database.entities.ExpenseEntity
import com.spendmindai.app.core.data.database.entities.RecurringExpenseEntity
import com.spendmindai.app.core.data.database.entities.UserPreferencesEntity
import java.util.Date

@Database(
    entities = [
        ExpenseEntity::class,
        CategoryEntity::class,
        RecurringExpenseEntity::class,
        UserPreferencesEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SpendMindDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringExpenseDao(): RecurringExpenseDao
    abstract fun userPreferencesDao(): UserPreferencesDao

    companion object {
        @Volatile
        private var INSTANCE: SpendMindDatabase? = null

        fun getDatabase(context: Context): SpendMindDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    SpendMindDatabase::class.java,
                    "spendmind_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}

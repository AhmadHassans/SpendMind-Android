package com.spendmindai.app.di

import android.content.Context
import com.spendmindai.app.core.data.database.SpendMindDatabase
import com.spendmindai.app.core.data.database.dao.CategoryDao
import com.spendmindai.app.core.data.database.dao.ExpenseDao
import com.spendmindai.app.core.data.database.dao.RecurringExpenseDao
import com.spendmindai.app.core.data.database.dao.UserPreferencesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SpendMindDatabase =
        SpendMindDatabase.getDatabase(context)

    @Provides
    fun provideExpenseDao(db: SpendMindDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideCategoryDao(db: SpendMindDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideRecurringExpenseDao(db: SpendMindDatabase): RecurringExpenseDao = db.recurringExpenseDao()

    @Provides
    fun provideUserPreferencesDao(db: SpendMindDatabase): UserPreferencesDao = db.userPreferencesDao()
}

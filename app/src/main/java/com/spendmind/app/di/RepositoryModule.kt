package com.spendmindai.app.di

import com.spendmindai.app.core.data.repository.CategoryRepository
import com.spendmindai.app.core.data.repository.ExpenseRepository
import com.spendmindai.app.core.data.repository.OnboardingRepository
import com.spendmindai.app.core.data.repository.RecurringExpenseRepository
import com.spendmindai.app.core.data.repository.UserPreferencesRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Repository module for Hilt dependency injection.
 *
 * All repositories are annotated with @Singleton and @Inject constructor,
 * so Hilt can provide them automatically without explicit @Binds or @Provides here.
 *
 * - [ExpenseRepository]          – manages expense CRUD via ExpenseDao
 * - [CategoryRepository]         – manages category CRUD and default seeding
 * - [UserPreferencesRepository]  – manages user preferences via Room and SharedPreferences
 * - [OnboardingRepository]       – tracks onboarding progress via SharedPreferences
 * - [RecurringExpenseRepository] – manages recurring expense CRUD via RecurringExpenseDao
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule

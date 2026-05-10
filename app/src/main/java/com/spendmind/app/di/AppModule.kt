package com.spendmindai.app.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @Named("spendmind_prefs")
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("spendmind_prefs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    @Named("onboarding_prefs")
    fun provideOnboardingPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
}

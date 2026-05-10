package com.spendmindai.app.core.data.repository

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class OnboardingRepository @Inject constructor(
    @Named("onboarding_prefs") private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome"
        private const val KEY_HAS_SEEN_CATEGORIES = "has_seen_categories"
        private const val KEY_HAS_SEEN_LANGUAGE = "has_seen_language"
        private const val KEY_HAS_SEEN_PERMISSIONS = "has_seen_permissions"
        private const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    }

    fun hasSeenWelcome(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_SEEN_WELCOME, false)

    fun setHasSeenWelcome(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_WELCOME, seen).apply()
    }

    fun hasSeenCategories(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_SEEN_CATEGORIES, false)

    fun setHasSeenCategories(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_CATEGORIES, seen).apply()
    }

    fun hasSeenLanguage(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_SEEN_LANGUAGE, false)

    fun setHasSeenLanguage(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_LANGUAGE, seen).apply()
    }

    fun hasSeenPermissions(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_SEEN_PERMISSIONS, false)

    fun setHasSeenPermissions(seen: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_SEEN_PERMISSIONS, seen).apply()
    }

    fun hasCompletedOnboarding(): Boolean =
        sharedPreferences.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)

    fun setHasCompletedOnboarding(completed: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, completed).apply()
    }

    fun resetOnboarding() {
        sharedPreferences.edit()
            .putBoolean(KEY_HAS_SEEN_WELCOME, false)
            .putBoolean(KEY_HAS_SEEN_CATEGORIES, false)
            .putBoolean(KEY_HAS_SEEN_LANGUAGE, false)
            .putBoolean(KEY_HAS_SEEN_PERMISSIONS, false)
            .putBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)
            .apply()
    }

    fun getOnboardingState(): OnboardingState = OnboardingState(
        hasSeenWelcome = hasSeenWelcome(),
        hasSeenCategories = hasSeenCategories(),
        hasSeenLanguage = hasSeenLanguage(),
        hasSeenPermissions = hasSeenPermissions(),
        hasCompletedOnboarding = hasCompletedOnboarding()
    )
}

data class OnboardingState(
    val hasSeenWelcome: Boolean,
    val hasSeenCategories: Boolean,
    val hasSeenLanguage: Boolean,
    val hasSeenPermissions: Boolean,
    val hasCompletedOnboarding: Boolean
)

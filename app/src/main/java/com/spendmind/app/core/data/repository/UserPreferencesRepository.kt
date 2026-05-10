package com.spendmindai.app.core.data.repository

import android.content.SharedPreferences
import com.spendmindai.app.core.data.database.dao.UserPreferencesDao
import com.spendmindai.app.core.data.database.entities.toEntity
import com.spendmindai.app.core.data.database.entities.toDomainModel
import com.spendmindai.app.core.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val userPreferencesDao: UserPreferencesDao,
    @Named("spendmind_prefs") private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_USER_ID = "user_id"
        private const val DEFAULT_PREFS_ID = "user_preferences"
    }

    fun getUserPreferences(): Flow<UserPreferences> =
        userPreferencesDao.getUserPreferences().map { entity ->
            entity?.toDomainModel() ?: UserPreferences.DEFAULT
        }

    suspend fun getUserPreferencesOnce(): UserPreferences =
        userPreferencesDao.getUserPreferencesOnce()?.toDomainModel() ?: UserPreferences.DEFAULT

    suspend fun updatePreferences(preferences: UserPreferences) {
        userPreferencesDao.insertOrUpdate(preferences.toEntity())
    }

    suspend fun updateCurrency(currency: String) {
        val current = getUserPreferencesOnce()
        updatePreferences(current.copy(currency = currency))
    }

    suspend fun updateLanguage(language: String?) {
        val current = getUserPreferencesOnce()
        updatePreferences(current.copy(language = language))
    }

    suspend fun updateMonthStartDay(day: Int) {
        val current = getUserPreferencesOnce()
        updatePreferences(current.copy(monthStartDay = day))
    }

    suspend fun updateNotificationSettings(
        enabled: Boolean,
        hour: Int,
        minute: Int
    ) {
        val current = getUserPreferencesOnce()
        updatePreferences(
            current.copy(
                notificationEnabled = enabled,
                notificationHour = hour,
                notificationMinute = minute
            )
        )
    }

    suspend fun updateAutoSubmitTranscription(autoSubmit: Boolean) {
        val current = getUserPreferencesOnce()
        updatePreferences(current.copy(autoSubmitTranscription = autoSubmit))
    }

    suspend fun ensurePreferencesExist() {
        val existing = userPreferencesDao.getUserPreferencesOnce()
        if (existing == null) {
            userPreferencesDao.insertOrUpdate(
                UserPreferences(id = DEFAULT_PREFS_ID).toEntity()
            )
        }
    }

    fun isFirstLaunch(): Boolean =
        sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)

    fun markFirstLaunchComplete() {
        sharedPreferences.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun getUserId(): String? =
        sharedPreferences.getString(KEY_USER_ID, null)

    fun setUserId(userId: String) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply()
    }
}

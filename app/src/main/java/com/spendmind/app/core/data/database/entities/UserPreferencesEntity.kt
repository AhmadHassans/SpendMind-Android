package com.spendmindai.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spendmindai.app.core.domain.model.UserPreferences
import java.util.Date

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey
    val id: String,
    val currency: String = "USD",
    val language: String? = null,
    val autoSubmitTranscription: Boolean = false,
    val monthStartDay: Int = 1,
    val notificationEnabled: Boolean = false,
    val notificationHour: Int = 21,
    val notificationMinute: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

fun UserPreferencesEntity.toDomainModel(): UserPreferences = UserPreferences(
    id = id,
    currency = currency,
    language = language,
    autoSubmitTranscription = autoSubmitTranscription,
    monthStartDay = monthStartDay,
    notificationEnabled = notificationEnabled,
    notificationHour = notificationHour,
    notificationMinute = notificationMinute,
    createdAt = Date(createdAt),
    updatedAt = Date(updatedAt)
)

fun UserPreferences.toEntity(): UserPreferencesEntity = UserPreferencesEntity(
    id = id,
    currency = currency,
    language = language,
    autoSubmitTranscription = autoSubmitTranscription,
    monthStartDay = monthStartDay,
    notificationEnabled = notificationEnabled,
    notificationHour = notificationHour,
    notificationMinute = notificationMinute,
    createdAt = createdAt.time,
    updatedAt = updatedAt.time
)

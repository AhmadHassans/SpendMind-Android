package com.spendmindai.app.core.infrastructure.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CrashlyticsService @Inject constructor() {
    private val crashlytics: FirebaseCrashlytics = Firebase.crashlytics

    fun logException(throwable: Throwable) { crashlytics.recordException(throwable) }
    fun logMessage(message: String) { crashlytics.log(message) }
    fun setCustomKey(key: String, value: Any) {
        when (value) {
            is String -> crashlytics.setCustomKey(key, value)
            is Int -> crashlytics.setCustomKey(key, value)
            is Long -> crashlytics.setCustomKey(key, value)
            is Double -> crashlytics.setCustomKey(key, value)
            is Float -> crashlytics.setCustomKey(key, value)
            is Boolean -> crashlytics.setCustomKey(key, value)
            else -> crashlytics.setCustomKey(key, value.toString())
        }
    }
    fun setUserId(userId: String) { crashlytics.setUserId(userId) }
    fun setCrashlyticsCollectionEnabled(enabled: Boolean) { crashlytics.setCrashlyticsCollectionEnabled(enabled) }
    fun sendUnsentReports() { crashlytics.sendUnsentReports() }
}

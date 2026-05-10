package com.spendmindai.app.core.infrastructure.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.spendmindai.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/** Channel ID shared between [NotificationService] and [DailyReminderReceiver]. */
const val CHANNEL_ID_EXPENSE_REMINDERS = "expense_reminders"
const val NOTIFICATION_ID_DAILY_REMINDER = 1001
const val ACTION_DAILY_REMINDER = "com.spendmindai.app.ACTION_DAILY_REMINDER"

@Singleton
class NotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // -------------------------------------------------------------------------
    // Channel setup
    // -------------------------------------------------------------------------

    /**
     * Creates the "expense_reminders" notification channel.
     * Safe to call multiple times — Android is idempotent for existing channels.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EXPENSE_REMINDERS,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to log your expenses"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    // -------------------------------------------------------------------------
    // Daily reminder scheduling
    // -------------------------------------------------------------------------

    /**
     * Schedules a daily reminder at [hourOfDay]:[minute] (24-hour clock).
     * Cancels any previously scheduled reminder before setting a new one.
     *
     * @param hourOfDay  Hour in 24-hour format (default 21 = 9 PM).
     * @param minute     Minute (default 0).
     */
    fun scheduleDailyReminder(hourOfDay: Int = 21, minute: Int = 0) {
        cancelDailyReminder()

        val triggerTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // If the time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

        val pendingIntent = buildReminderPendingIntent()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    /** Cancels the daily reminder alarm if one is scheduled. */
    fun cancelDailyReminder() {
        alarmManager.cancel(buildReminderPendingIntent())
    }

    // -------------------------------------------------------------------------
    // Immediate notification helper
    // -------------------------------------------------------------------------

    /** Posts a single expense reminder notification immediately. */
    fun showDailyReminderNotification() {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Don't forget to log your expenses!")
            .setContentText("Tap to record today's spending in SpendMind.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID_DAILY_REMINDER, notification)
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private fun buildReminderPendingIntent(): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = ACTION_DAILY_REMINDER
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, NOTIFICATION_ID_DAILY_REMINDER, intent, flags)
    }
}

// -------------------------------------------------------------------------
// BroadcastReceiver – fired by AlarmManager each day
// -------------------------------------------------------------------------

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_REMINDER &&
            intent.action != Intent.ACTION_BOOT_COMPLETED
        ) return

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Ensure channel exists (needed after device reboot)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID_EXPENSE_REMINDERS,
                "Expense Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminders to log your expenses"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_EXPENSE_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Don't forget to log your expenses!")
            .setContentText("Tap to record today's spending in SpendMind.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            notificationManager.notify(NOTIFICATION_ID_DAILY_REMINDER, notification)
        }

        // Re-schedule for the next day (exact alarms are one-shot on Android)
        if (intent.action == ACTION_DAILY_REMINDER) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val nextTrigger = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.timeInMillis

            val pendingFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID_DAILY_REMINDER,
                Intent(context, DailyReminderReceiver::class.java).apply {
                    action = ACTION_DAILY_REMINDER
                },
                pendingFlags
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent)
            }
        }
    }
}

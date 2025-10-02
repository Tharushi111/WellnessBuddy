package com.example.wellnessbuddy

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class HydrationReminderManager(private val context: Context) {

    companion object {
        private const val WORK_NAME = "hydration_reminder_work"
    }

    /**
     * Schedule periodic hydration reminders
     */
    fun scheduleReminder(intervalMinutes: Int) {
        val workRequest = PeriodicWorkRequestBuilder<HydrationWorker>(
            intervalMinutes.toLong(),
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    /**
     * Cancel hydration reminders
     */
    fun cancelReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Check if reminders are scheduled
     */
    fun isReminderScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WORK_NAME)
            .get()

        return workInfos.any { !it.state.isFinished }
    }
}
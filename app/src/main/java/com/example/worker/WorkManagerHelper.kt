package com.example.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.model.TaskItem
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManagerHelper configures and enqueues triggers for task deadline notifications.
 */
object WorkManagerHelper {

    private const val PERIODIC_CHECK_WORK_NAME = "myevo_periodic_deadline_check"
    private const val UNIQUE_TASK_WORK_PREFIX = "myevo_task_reminder_"

    /**
     * Schedules a one-time reminder for a specific task based on its dueDate, dueTime,
     * and reminderMinutesBefore.
     */
    fun scheduleTaskDeadlineReminder(context: Context, task: TaskItem) {
        if (task.isCompleted) {
            cancelTaskReminder(context, task.id)
            return
        }

        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val deadlineDate = dateFormat.parse("${task.dueDate} ${task.dueTime}") ?: return
            val now = System.currentTimeMillis()
            val reminderTime = deadlineDate.time - (task.reminderMinutesBefore * 60 * 1000L)
            val initialDelayMillis = (reminderTime - now).coerceAtLeast(0L)

            val inputData = workDataOf(TaskDeadlineReminderWorker.KEY_TASK_ID to task.id)

            val workRequest = OneTimeWorkRequestBuilder<TaskDeadlineReminderWorker>()
                .setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("task_${task.id}")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$UNIQUE_TASK_WORK_PREFIX${task.id}",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        } catch (_: Exception) {
            // Safe fallback if date parsing fails
        }
    }

    /**
     * Cancels scheduled reminder for a specific task.
     */
    fun cancelTaskReminder(context: Context, taskId: Long) {
        try {
            WorkManager.getInstance(context).cancelUniqueWork("$UNIQUE_TASK_WORK_PREFIX$taskId")
        } catch (_: Exception) {
            // Ignore
        }
    }

    /**
     * Schedules a periodic background check (every 15 mins) across all approaching tasks.
     */
    fun schedulePeriodicDeadlineCheck(context: Context) {
        try {
            val periodicWork = PeriodicWorkRequestBuilder<TaskDeadlineReminderWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .addTag(PERIODIC_CHECK_WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_CHECK_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWork
            )
        } catch (_: Exception) {
            // Ignore
        }
    }
}

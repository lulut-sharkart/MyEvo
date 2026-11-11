package com.example.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.MainActivity
import com.example.R
import com.example.data.local.MyEvoDatabase
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * TaskDeadlineReminderWorker checks approaching task deadlines in Room database
 * and triggers high-priority local notifications.
 */
class TaskDeadlineReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "myevo_task_deadlines"
        const val CHANNEL_NAME = "Pengingat Tenggat Tugas MyEvo"
        const val KEY_TASK_ID = "key_task_id"
    }

    override suspend fun doWork(): Result {
        createNotificationChannel()

        val specificTaskId = inputData.getLong(KEY_TASK_ID, -1L)
        val database = MyEvoDatabase.getDatabase(context)
        val allTasks = database.taskDao().getAllTasks().firstOrNull() ?: emptyList()

        if (specificTaskId != -1L) {
            val task = allTasks.find { it.id == specificTaskId }
            if (task != null && !task.isCompleted) {
                sendTaskNotification(task)
            }
        } else {
            // Check all approaching tasks (due within reminder window)
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (task in allTasks) {
                if (task.isCompleted) continue
                try {
                    val dateString = "${task.dueDate} ${task.dueTime}"
                    val deadlineDate = dateFormat.parse(dateString)
                    if (deadlineDate != null) {
                        val diffMillis = deadlineDate.time - now
                        val reminderWindowMillis = task.reminderMinutesBefore * 60 * 1000L
                        // Trigger if within reminder window and deadline is in future or overdue by < 30 mins
                        if (diffMillis in -1800000L..reminderWindowMillis) {
                            sendTaskNotification(task)
                        }
                    }
                } catch (_: Exception) {
                    // Ignore parse error and continue
                }
            }
        }

        return Result.success()
    }

    private fun sendTaskNotification(task: TaskItem) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityLabel = when (task.priority) {
            TaskPriority.HIGH -> "⚡ PRIORITAS TINGGI"
            TaskPriority.MEDIUM -> "📌 Prioritas Sedang"
            TaskPriority.LOW -> "🔹 Prioritas Rendah"
        }

        val contentText = "Tenggat waktu: ${task.dueDate} pukul ${task.dueTime} ($priorityLabel)"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("⏰ Tenggat Mendekati: ${task.title}")
            .setContentText(contentText)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$contentText\n${task.description.ifBlank { "Segera selesaikan tugas ini agar produktivitas harian Anda tetap terjaga!" }}")
            )
            .setPriority(if (task.priority == TaskPriority.HIGH) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(task.id.toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi pengingat tenggat waktu tugas harian MyEvo"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

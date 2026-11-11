package com.example.data.repository

import android.content.Context
import com.example.data.local.TaskDao
import com.example.data.model.TaskItem
import com.example.data.model.TaskPriority
import com.example.worker.WorkManagerHelper
import kotlinx.coroutines.flow.Flow

/**
 * TaskRepository handles data abstraction between Room TaskDao and the ViewModels,
 * providing a clean, consistent API for CRUD operations on tasks across MyEvo and
 * scheduling WorkManager deadline triggers.
 */
class TaskRepository(
    private val taskDao: TaskDao,
    private val context: Context? = null
) {

    val allTasks: Flow<List<TaskItem>> = taskDao.getAllTasks()

    suspend fun insertTask(task: TaskItem): Long {
        val id = taskDao.insertTask(task)
        val createdTask = task.copy(id = id)
        context?.let { ctx ->
            WorkManagerHelper.scheduleTaskDeadlineReminder(ctx, createdTask)
        }
        if (task.priority == TaskPriority.HIGH) {
            SyncManager.pushCustomNotification(
                title = "⚡ Pengingat Prioritas: ${task.title}",
                message = "Tenggat waktu ${task.dueDate} pukul ${task.dueTime}.",
                type = "REMINDER"
            )
        }
        return id
    }

    suspend fun updateTask(task: TaskItem) {
        taskDao.updateTask(task)
        context?.let { ctx ->
            if (task.isCompleted) {
                WorkManagerHelper.cancelTaskReminder(ctx, task.id)
            } else {
                WorkManagerHelper.scheduleTaskDeadlineReminder(ctx, task)
            }
        }
    }

    suspend fun deleteTask(task: TaskItem) {
        taskDao.deleteTask(task)
        context?.let { ctx ->
            WorkManagerHelper.cancelTaskReminder(ctx, task.id)
        }
    }

    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        taskDao.setTaskCompleted(id, completed)
        context?.let { ctx ->
            if (completed) {
                WorkManagerHelper.cancelTaskReminder(ctx, id)
            }
        }
    }

    suspend fun clearAllTasks() {
        taskDao.clearAllTasks()
    }
}

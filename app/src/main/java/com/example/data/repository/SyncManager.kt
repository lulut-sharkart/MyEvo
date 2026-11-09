package com.example.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PushNotification(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: String, // "DEADLINE", "REMINDER", "CHALLENGE", "SYNC", "BUDGET"
    val isRead: Boolean = false
)

object SyncManager {
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(getFormattedCurrentTime())
    val lastSyncTime = _lastSyncTime.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            PushNotification(
                id = "welcome_1",
                title = "👋 Selamat Datang di MyEvo!",
                message = "Asisten cerdas harian all-in-one Anda telah siap. Mulai dengan menjadwalkan tugas atau target anggaran Anda.",
                timeAgo = "Baru saja",
                type = "REMINDER"
            )
        )
    )
    val notifications = _notifications.asStateFlow()

    fun toggleOnlineStatus() {
        val newStatus = !_isOnline.value
        _isOnline.value = newStatus
        if (newStatus) {
            triggerSync()
        }
    }

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
        if (online) {
            triggerSync()
        }
    }

    fun triggerSync() {
        _isSyncing.value = true
        _lastSyncTime.value = getFormattedCurrentTime()
        _isSyncing.value = false
    }

    fun pushCustomNotification(title: String, message: String, type: String = "REMINDER") {
        val newNotif = PushNotification(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            timeAgo = "Baru saja",
            type = type
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    private fun getFormattedCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm, dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}

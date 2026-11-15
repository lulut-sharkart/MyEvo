package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PinSecurityDataStore
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MyEvoViewModel
import kotlinx.coroutines.launch

@Composable
fun AdminScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val securityStore = remember { PinSecurityDataStore(context) }
    val hasAdminPassword by securityStore.hasAdminPassword.collectAsState(initial = true)

    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val notifTitle by viewModel.adminNotificationTitle.collectAsState()
    val notifMsg by viewModel.adminNotificationMsg.collectAsState()

    val tasks by viewModel.tasks.collectAsState()
    val finances by viewModel.financeEntries.collectAsState()
    val debts by viewModel.debtsLoans.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val events by viewModel.events.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val supabaseStatus = remember { viewModel.supabaseConfigStatus }

    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    if (!isAuthenticated) {
        // Zero-visibility Admin Setup or Verification View (password never revealed or hinted)
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(NeonIndigo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (hasAdminPassword) "Panel Kontrol Administrator" else "Buat Kata Sandi Admin Rahasia",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (hasAdminPassword) {
                            "Masukkan kata sandi administrator rahasia. Hash SHA-256 diverifikasi secara tertutup dan tidak dapat dilihat oleh siapa pun."
                        } else {
                            "Belum ada kata sandi admin. Buat kata sandi minimal 6 karakter; hanya hash kriptografis yang disimpan di DataStore."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            authError = null
                        },
                        label = { Text("Kata Sandi Admin Rahasia") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )

                    if (!hasAdminPassword) {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirmPasswordInput,
                            onValueChange = {
                                confirmPasswordInput = it
                                authError = null
                            },
                            label = { Text("Konfirmasi Kata Sandi Admin") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_confirm_pin_input")
                        )
                    }

                    if (authError != null) {
                        Text(
                            text = authError ?: "",
                            color = PriorityHigh,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                if (!hasAdminPassword) {
                                    if (passwordInput.length < 6) {
                                        authError = "Kata sandi admin minimal 6 karakter."
                                    } else if (passwordInput != confirmPasswordInput) {
                                        authError = "Konfirmasi kata sandi tidak cocok."
                                    } else {
                                        securityStore.saveAdminPassword(passwordInput)
                                        passwordInput = ""
                                        confirmPasswordInput = ""
                                        viewModel.markAdminAuthenticated(true)
                                    }
                                } else {
                                    val ok = securityStore.verifyAdminPassword(passwordInput)
                                    passwordInput = ""
                                    if (ok) {
                                        authError = null
                                        viewModel.markAdminAuthenticated(true)
                                    } else {
                                        authError = "Akses ditolak. Kata sandi administrator tidak cocok."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_submit_btn")
                    ) {
                        Text(
                            text = if (hasAdminPassword) "Verifikasi & Masuk" else "Simpan & Buka Panel Admin",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = { viewModel.navigateTo(AppScreen.HOME) }) {
                        Text("Kembali ke Beranda")
                    }
                }
            }
        }
    } else {
        val totalRoomRecords = tasks.size + finances.size + debts.size + notes.size + events.size
        val unlockedBadgesCount = badges.count { it.isUnlocked }
        val completedTasksCount = tasks.count { it.isCompleted }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.navigateTo(AppScreen.PROFILE) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                        }
                        Text(
                            text = "Admin Hub MyEvo",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = { viewModel.logoutAdmin() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PriorityHigh.copy(alpha = 0.15f),
                            contentColor = PriorityHigh
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kunci Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Authentic Local Room & Cloud Telemetry Grid (zero dummy numbers)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Storage, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Data Room Lokal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$totalRoomRecords", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("${tasks.size} tugas • ${finances.size + debts.size} finansial", fontSize = 10.sp, color = ElectricCyan)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Capaian Riil", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("$completedTasksCount / ${tasks.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldGreen)
                            Text("$unlockedBadgesCount/${badges.size} lencana terbuka", fontSize = 10.sp, color = AmberGold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudDone, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Koneksi Cloud", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = if (isOnline) EmeraldGreen else AmberGold
                            )
                            Text(
                                text = if (supabaseStatus.isConfigured) "Supabase siap" else "Secrets belum diisi",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Keamanan Sandi", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("SHA-256", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AmberGold)
                            Text("Zero-Visibility Hash", fontSize = 10.sp, color = EmeraldGreen)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { showChangePasswordDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = NeonIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubah Kata Sandi Admin Rahasia", color = NeonIndigo, fontWeight = FontWeight.SemiBold)
                }
            }

            // Push Notification Broadcaster Card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_broadcast_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = CoralOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kirim Pengumuman / Notifikasi Lokal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = notifTitle,
                            onValueChange = { viewModel.setAdminNotificationTitle(it) },
                            label = { Text("Judul Notifikasi Pengingat") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_notif_title_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notifMsg,
                            onValueChange = { viewModel.setAdminNotificationMsg(it) },
                            label = { Text("Pesan Pengumuman") },
                            maxLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_notif_msg_input")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.broadcastAdminNotification() },
                            colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_send_broadcast_btn")
                        ) {
                            Text("Kirim Notifikasi Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Authentic Activity / Notification Log (replaces removed dummy tickets)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Log Aktivitas & Notifikasi Sistem (${notifications.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (notifications.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Belum ada log aktivitas atau notifikasi sistem.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(notifications.take(10)) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(NeonIndigo.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = notif.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusPill(text = notif.type, color = ElectricCyan)
                        }
                    }
                }
            }
        }
    }

    if (showChangePasswordDialog) {
        var curPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }
        var changeErr by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Ubah Kata Sandi Admin", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = curPass,
                        onValueChange = { curPass = it; changeErr = null },
                        label = { Text("Kata Sandi Saat Ini") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it; changeErr = null },
                        label = { Text("Kata Sandi Baru (min. 6 karakter)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it; changeErr = null },
                        label = { Text("Konfirmasi Kata Sandi Baru") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (changeErr != null) {
                        Text(changeErr ?: "", color = PriorityHigh, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            if (newPass.length < 6) {
                                changeErr = "Minimal 6 karakter."
                            } else if (newPass != confirmPass) {
                                changeErr = "Konfirmasi tidak cocok."
                            } else {
                                val changed = securityStore.changeAdminPassword(curPass, newPass)
                                if (changed) {
                                    showChangePasswordDialog = false
                                } else {
                                    changeErr = "Kata sandi saat ini salah."
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) { Text("Batal") }
            }
        )
    }
}

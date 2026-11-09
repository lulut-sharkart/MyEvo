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
import androidx.compose.material.icons.automirrored.filled.ContactSupport
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MyEvoViewModel

@Composable
fun AdminScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val isAuthenticated by viewModel.isAdminAuthenticated.collectAsState()
    val notifTitle by viewModel.adminNotificationTitle.collectAsState()
    val notifMsg by viewModel.adminNotificationMsg.collectAsState()

    var adminPinInput by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf(false) }

    val supportTickets = listOf(
        Pair("Budi Gunawan", "Pertanyaan seputar sinkronisasi kalender Google"),
        Pair("Alya Putri", "Saran penambahan kosa kata baru bahasa Korea"),
        Pair("Dimas Anggara", "Pertanyaan backup database cloud offline")
    )

    if (!isAuthenticated) {
        // Admin Login View
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
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Panel Kontrol Administrator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Akses terbatas untuk monitoring analitik pengguna dan siaran notifikasi push.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            loginError = false
                        },
                        label = { Text("Kata Sandi Admin (admin123)") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_pin_input")
                    )

                    if (loginError) {
                        Text(
                            text = "Kata sandi admin salah! Gunakan: admin123",
                            color = PriorityHigh,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (viewModel.authenticateAdmin(adminPinInput)) {
                                loginError = false
                            } else {
                                loginError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_submit_btn")
                    ) {
                        Text("Masuk Sebagai Admin", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Kembali ke Beranda")
                    }
                }
            }
        }
    } else {
        // Authenticated Admin Dashboard
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh.copy(alpha = 0.15f), contentColor = PriorityHigh),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Keluar Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Real-Time System Metrics Grid
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
                                Icon(Icons.Default.Group, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pengguna Aktif", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("14,290", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("+8.4% minggu ini", fontSize = 10.sp, color = EmeraldGreen)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Dns, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Server Health", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("99.98%", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = EmeraldGreen)
                            Text("Latensi 28ms", fontSize = 10.sp, color = ElectricCyan)
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
                                Text("Sync Queue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("0 Tertunda", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = EmeraldGreen)
                            Text("Realtime synced", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                Text("Enkripsi E2E", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("AES-256", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AmberGold)
                            Text("Zero-Knowledge", fontSize = 10.sp, color = EmeraldGreen)
                        }
                    }
                }
            }

            // Push Notification Broadcaster Card
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
                                text = "Kirim Notifikasi Push Global / Pengingat",
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
                            label = { Text("Pesan Siaran ke Pengguna") },
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
                                .testTag("admin_send_broadcast_btn")
                        ) {
                            Text("Siarkan Notifikasi Push Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Customer Support Tickets
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Laporan & Tiket Bantuan Pelanggan (3):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(supportTickets) { (user, issue) ->
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
                            Icon(Icons.AutoMirrored.Filled.ContactSupport, contentDescription = null, tint = NeonIndigo, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = user, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = issue, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusPill(text = "Terbuka", color = AmberGold)
                    }
                }
            }
        }
    }
}

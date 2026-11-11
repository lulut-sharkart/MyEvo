package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppLockPreferences
import com.example.data.repository.DataExportHelper
import com.example.data.repository.HolidayProvider
import com.example.ui.components.AppHeader
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
fun SettingsScreen(
    viewModel: MyEvoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware and gesture back
    BackHandler { onBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSync by viewModel.lastSyncTime.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val financeEntries by viewModel.financeEntries.collectAsState()
    val debtsLoans by viewModel.debtsLoans.collectAsState()

    val appLockPrefs = remember { AppLockPreferences(context) }
    val isAppLockActive by appLockPrefs.isAppLockEnabled.collectAsState(initial = false)
    val hasConfiguredPin by appLockPrefs.hasConfiguredPin.collectAsState(initial = false)

    var showPinChangeModal by remember { mutableStateOf(false) }
    var showBudgetChangeModal by remember { mutableStateOf(false) }
    var showClearDataModal by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top Bar with explicit Back Button
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("btn_settings_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke Beranda",
                            tint = NeonIndigo
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Pengaturan MyEvo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Konfigurasi tampilan, keamanan, notifikasi & cloud",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusPill(
                        text = if (isOnline) "Online" else "Offline",
                        color = if (isOnline) EmeraldGreen else AmberGold
                    )
                }
            }
        }

        // Notification / Feedback Banner
        if (statusMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldGreen,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { statusMessage = null }) {
                            Text("Tutup", fontSize = 11.sp, color = EmeraldGreen)
                        }
                    }
                }
            }
        }

        // SECTION 1: TAMPILAN & TEMA
        item {
            SettingsCategoryHeader(title = "Tampilan & Gaya Visual", icon = Icons.Default.Palette)
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Mode Gelap (Dark Mode)",
                    subtitle = "Tampilan modern dengan kontras tinggi yang nyaman di mata",
                    checked = userProfile.isDarkMode,
                    onCheckedChange = { viewModel.toggleDarkMode() },
                    testTag = "switch_dark_mode"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Mode Baca Redup (Night Reading)",
                    subtitle = "Filter cahaya hangat yang menenangkan saat membaca malam",
                    checked = userProfile.isReadingMode,
                    onCheckedChange = { viewModel.toggleReadingMode() },
                    testTag = "switch_reading_mode"
                )
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bahasa Antarmuka", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Pilih bahasa aplikasi utama", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = userProfile.appLanguage == "id",
                            onClick = { viewModel.setAppLanguage("id") },
                            label = { Text("Indonesia", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = userProfile.appLanguage == "en",
                            onClick = { viewModel.setAppLanguage("en") },
                            label = { Text("English", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // SECTION 2: KEAMANAN & PRIVASI
        item {
            SettingsCategoryHeader(title = "Keamanan & Kunci Aplikasi", icon = Icons.Default.Security)
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Kunci Aplikasi (App Lock PIN)",
                    subtitle = if (isAppLockActive) "Memerlukan 4 digit PIN saat membuka aplikasi" else "Tidak memerlukan PIN untuk masuk",
                    checked = isAppLockActive,
                    onCheckedChange = { checked ->
                        if (checked && !hasConfiguredPin) {
                            showPinChangeModal = true
                        } else {
                            coroutineScope.launch {
                                appLockPrefs.setAppLockEnabled(checked)
                                viewModel.toggleAppLock(checked)
                            }
                        }
                    },
                    testTag = "switch_app_lock"
                )
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ubah Kode PIN Keamanan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (hasConfiguredPin) "PIN 4 digit telah terpasang" else "Belum ada PIN yang diatur",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showPinChangeModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_change_pin")
                    ) {
                        Text("Atur PIN", fontSize = 12.sp)
                    }
                }
            }
        }

        // SECTION 3: NOTIFIKASI CERDAS & PENGINGAT
        item {
            SettingsCategoryHeader(title = "Notifikasi Cerdas & Pengingat", icon = Icons.Default.Notifications)
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Pengingat Tugas Tenggat Waktu",
                    subtitle = "Kirim notifikasi otomatis sebelum batas waktu tugas berakhir",
                    checked = userProfile.isTaskReminderEnabled,
                    onCheckedChange = { viewModel.toggleTaskReminder() },
                    testTag = "switch_task_reminders"
                )
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Waktu Peringatan Default", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Seberapa awal pengingat dibunyikan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(15, 30, 60).forEach { mins ->
                            FilterChip(
                                selected = userProfile.defaultReminderMinutes == mins,
                                onClick = { viewModel.setDefaultReminderMinutes(mins) },
                                label = { Text("${mins}m", fontSize = 10.sp) }
                            )
                        }
                    }
                }
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Pengingat Belajar Bahasa Harian",
                    subtitle = "Notifikasi cerdas agar progres latihan kosakata tetap konsisten",
                    checked = userProfile.isStudyReminderEnabled,
                    onCheckedChange = { viewModel.toggleStudyReminder() },
                    testTag = "switch_study_reminders"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Notifikasi Tantangan Mingguan",
                    subtitle = "Pemberitahuan saat ada kompetisi dan misi XP baru",
                    checked = userProfile.isChallengeReminderEnabled,
                    onCheckedChange = { viewModel.toggleChallengeReminder() },
                    testTag = "switch_challenge_reminders"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Getar Notifikasi",
                    subtitle = "Getaran haptic saat notifikasi pengingat muncul",
                    checked = userProfile.isVibrationEnabled,
                    onCheckedChange = { viewModel.toggleVibration() },
                    testTag = "switch_vibration"
                )
                SettingsDivider()
                SettingsSwitchRow(
                    title = "Suara Peringatan",
                    subtitle = "Putar nada dering lembut saat pengingat aktif",
                    checked = userProfile.isSoundEnabled,
                    onCheckedChange = { viewModel.toggleSound() },
                    testTag = "switch_sound"
                )
            }
        }

        // SECTION 4: SINKRONISASI & CADANGAN CLOUD
        item {
            SettingsCategoryHeader(title = "Penyimpanan Cloud & Sinkronisasi", icon = Icons.Default.CloudSync)
        }

        item {
            SettingsCard {
                SettingsSwitchRow(
                    title = "Sinkronisasi Otomatis",
                    subtitle = "Unggah otomatis data tugas & keuangan saat internet aktif",
                    checked = userProfile.isAutoSyncEnabled,
                    onCheckedChange = { viewModel.toggleAutoSync() },
                    testTag = "switch_auto_sync"
                )
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cadangkan Data Sekarang", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Terakhir disinkronkan: $lastSync", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = {
                            viewModel.triggerCloudBackup()
                            statusMessage = "Pencadangan cloud berhasil dijalankan!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isOnline) NeonIndigo else AmberGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_sync_now")
                    ) {
                        Text(if (isSyncing) "Menyinkronkan..." else "Sinkron", fontSize = 12.sp)
                    }
                }
            }
        }

        // SECTION 5: KEUANGAN & ANGGARAN
        item {
            SettingsCategoryHeader(title = "Anggaran & Preferensi Keuangan", icon = Icons.Default.MonetizationOn)
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Target Anggaran Bulanan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        val formattedBudget = java.text.NumberFormat.getIntegerInstance(java.util.Locale.GERMANY).format(userProfile.monthlyBudgetGoal.toLong())
                        Text("Saat ini: Rp $formattedBudget", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                    }
                    Button(
                        onClick = { showBudgetChangeModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_change_budget")
                    ) {
                        Text("Ubah Target", fontSize = 12.sp)
                    }
                }
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Mata Uang Utama", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Simbol pengeluaran dan pemasukan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("IDR", "USD", "EUR", "JPY").forEach { curr ->
                            FilterChip(
                                selected = userProfile.currencyCode == curr,
                                onClick = { viewModel.setCurrency(curr) },
                                label = { Text(curr, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }
        }

        // SECTION 6: KALENDER & HARI LIBUR NASIONAL
        item {
            SettingsCategoryHeader(title = "Kalender & Hari Libur Nasional", icon = Icons.Default.Public)
        }

        item {
            SettingsCard {
                Text(
                    text = "Pilih Negara Kalender Otomatis:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(HolidayProvider.supportedCountries) { country ->
                        val isSelected = userProfile.countryCode == country.code
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setCountry(country.code, country.name)
                                statusMessage = "Kalender berhasil disesuaikan untuk ${country.name} (${country.code})"
                            },
                            label = { Text("${country.flagEmoji} ${country.code}", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // SECTION 7: EKSPOR DATA KE BERKAS CSV
        item {
            SettingsCategoryHeader(title = "Ekspor Data ke Berkas CSV", icon = Icons.Default.TableChart)
        }

        item {
            SettingsCard {
                // Export Tasks
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ekspor Riwayat Tugas (CSV)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${tasks.size} tugas tersimpan (judul, kategori, prioritas, tenggat & status)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val csv = DataExportHelper.generateTasksCsv(tasks)
                            val file = DataExportHelper.exportAndShareCsv(context, "myevo_tugas", csv)
                            statusMessage = "Berkas CSV riwayat tugas berhasil diekspor (${file.name}) ke Dokumen!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_export_tasks_csv")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor", fontSize = 12.sp)
                    }
                }

                SettingsDivider()

                // Export Finance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Ekspor Catatan Keuangan (CSV)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${financeEntries.size} transaksi & ${debtsLoans.size} catatan utang/piutang",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = {
                            val csv = DataExportHelper.generateFinanceCsv(financeEntries, debtsLoans)
                            val file = DataExportHelper.exportAndShareCsv(context, "myevo_keuangan", csv)
                            statusMessage = "Berkas CSV catatan keuangan berhasil diekspor (${file.name}) ke Dokumen!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_export_finance_csv")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor", fontSize = 12.sp)
                    }
                }
            }
        }

        // SECTION 8: MANAJEMEN DATA & CACHE
        item {
            SettingsCategoryHeader(title = "Penyimpanan & Pemeliharaan Data", icon = Icons.Default.Delete)
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bersihkan Cache Gambar & AI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Hapus berkas sementara untuk menghemat ruang", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.clearImageCache()
                            statusMessage = "Cache gambar dan berkas sementara telah dibersihkan!"
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_clear_cache")
                    ) {
                        Text("Bersihkan", fontSize = 11.sp)
                    }
                }
                SettingsDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hapus Semua Data Lokal", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PriorityHigh)
                        Text("Reset seluruh tugas, keuangan, dan catatan", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showClearDataModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_reset_data")
                    ) {
                        Text("Reset", fontSize = 11.sp)
                    }
                }
            }
        }

        // SECTION 8: AKUN & TENTANG MYEVO
        item {
            SettingsCategoryHeader(title = "Akun & Tentang Aplikasi", icon = Icons.Default.Info)
        }

        item {
            SettingsCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = NeonIndigo.copy(alpha = 0.15f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(userProfile.avatarEmoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userProfile.name.isNotBlank()) userProfile.name else "Pengguna MyEvo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (userProfile.isGuest) "Mode Tamu (Offline-Only)" else userProfile.emailOrPhone,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                SettingsDivider()
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("btn_logout_settings")
                ) {
                    Text(if (userProfile.isGuest) "Keluar dari Mode Tamu" else "Keluar / Ganti Akun")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "MyEvo Smart Productivity Suite • v1.0.0 (Build 36)\nOffline-First Architecture with Smart Cloud Sync & Gemini AI Integration",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    // MODAL: Ubah PIN Keamanan
    if (showPinChangeModal) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinChangeModal = false },
            title = { Text("Atur PIN Keamanan (4 Digit)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Masukkan 4 digit angka sebagai kode sandi keamanan aplikasi:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) newPin = it },
                        label = { Text("PIN Baru (4 Digit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_new_pin")
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) confirmPin = it },
                        label = { Text("Konfirmasi PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_confirm_pin")
                    )
                    if (pinError != null) {
                        Text(pinError ?: "", color = PriorityHigh, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length != 4) {
                            pinError = "PIN harus tepat 4 digit angka!"
                        } else if (newPin != confirmPin) {
                            pinError = "Konfirmasi PIN tidak cocok!"
                        } else {
                            coroutineScope.launch {
                                appLockPrefs.setPin(newPin)
                                appLockPrefs.setAppLockEnabled(true)
                                viewModel.toggleAppLock(true)
                                statusMessage = "PIN keamanan 4 digit berhasil disimpan dan diaktifkan!"
                                showPinChangeModal = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    modifier = Modifier.testTag("btn_save_pin")
                ) {
                    Text("Simpan PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinChangeModal = false }) { Text("Batal") }
            }
        )
    }

    // MODAL: Ubah Target Anggaran
    if (showBudgetChangeModal) {
        var budgetInput by remember { mutableStateOf(userProfile.monthlyBudgetGoal.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showBudgetChangeModal = false },
            title = { Text("Ubah Target Anggaran Bulanan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Tentukan batas maksimal rencana pengeluaran bulanan Anda:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) budgetInput = it },
                        label = { Text("Nominal Anggaran (Rp)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_budget_nominal")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = budgetInput.toDoubleOrNull() ?: 3000000.0
                        viewModel.updateMonthlyBudget(amount)
                        statusMessage = "Target anggaran bulanan berhasil diperbarui!"
                        showBudgetChangeModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    modifier = Modifier.testTag("btn_save_budget")
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBudgetChangeModal = false }) { Text("Batal") }
            }
        )
    }

    // MODAL: Konfirmasi Reset Data
    if (showClearDataModal) {
        AlertDialog(
            onDismissRequest = { showClearDataModal = false },
            title = { Text("Hapus Seluruh Data?", fontWeight = FontWeight.Bold, color = PriorityHigh) },
            text = {
                Text("Tindakan ini akan menghapus semua tugas, catatan terenkripsi, riwayat transaksi keuangan, dan reset pengaturan lokal ke kondisi awal.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllUserData()
                        statusMessage = "Semua data pengguna telah direset!"
                        showClearDataModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityHigh),
                    modifier = Modifier.testTag("btn_confirm_reset_data")
                ) {
                    Text("Ya, Hapus Semua")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataModal = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun SettingsCategoryHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonIndigo,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = NeonIndigo
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    )
}

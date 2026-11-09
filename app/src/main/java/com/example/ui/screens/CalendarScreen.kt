package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleEvent
import com.example.data.repository.HolidayProvider
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.MyEvoViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val events by viewModel.events.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()

    var showCountryMenu by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    // Calendar Days Generator for November 2026
    val currentMonthDays = remember {
        val list = mutableListOf<String>()
        for (i in 1..30) {
            val dayStr = if (i < 10) "0$i" else "$i"
            list.add("2026-11-$dayStr")
        }
        list
    }

    val dayEvents = remember(events, selectedDate) {
        events.filter { it.date == selectedDate }
    }

    val holidaysCount = remember(events) {
        events.count { it.isHoliday }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Kalender Interaktif",
                    subtitle = if (userProfile.isGuest) "Mode Tamu • Hari Libur Otomatis" else "Jadwal harian & libur nasional otomatis",
                    isOnline = isOnline,
                    isSyncing = isSyncing,
                    isDarkMode = userProfile.isDarkMode,
                    isReadingMode = userProfile.isReadingMode,
                    isGuest = userProfile.isGuest,
                    onToggleOnline = { viewModel.toggleOnlineMode() },
                    onSyncClick = { viewModel.triggerCloudBackup() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleReadingMode = { viewModel.toggleReadingMode() },
                    onGuestBannerClick = { viewModel.navigateTo(com.example.viewmodel.AppScreen.PROFILE) }
                )
            }

            // Country Selector & Holiday Auto-Sync Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("country_holiday_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Country",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hari Libur Nasional Lokasi:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { showCountryMenu = true }
                                        .testTag("country_selector_btn")
                                ) {
                                    Text(
                                        text = userProfile.selectedCountry,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showCountryMenu,
                                    onDismissRequest = { showCountryMenu = false }
                                ) {
                                    HolidayProvider.supportedCountries.forEach { country ->
                                        DropdownMenuItem(
                                            text = {
                                                Text("${country.flagEmoji} ${country.name} (${country.code})")
                                            },
                                            onClick = {
                                                viewModel.setCountry(country.code, country.name)
                                                showCountryMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Menampilkan $holidaysCount hari libur nasional resmi untuk ${userProfile.selectedCountry}. Kalender secara otomatis menyesuaikan hari libur bila lokasi diubah.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Google Calendar Sync Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("google_calendar_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4285F4).copy(alpha = 0.10f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4285F4)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "GCal",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Calendar Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Sinkronisasi 2 arah jadwal rapat & acara",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.syncGoogleCalendar() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("sync_google_cal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Sync",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sinkronkan", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Interactive Day Selector Carousel
            item {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "Pilih Tanggal (November 2026):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentMonthDays) { dateStr ->
                            val isSelected = selectedDate == dateStr
                            val dayNumber = dateStr.substring(8)
                            val hasHoliday = events.any { it.date == dateStr && it.isHoliday }
                            val hasEvent = events.any { it.date == dateStr && !it.isHoliday }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) NeonIndigo else MaterialTheme.colorScheme.surface,
                                border = if (hasHoliday) androidx.compose.foundation.BorderStroke(1.dp, PriorityHigh) else null,
                                modifier = Modifier
                                    .width(54.dp)
                                    .height(72.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.setSelectedCalendarDate(dateStr) }
                                    .testTag("day_btn_$dayNumber")
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Nov",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dayNumber,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else if (hasHoliday) PriorityHigh else MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        if (hasHoliday) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else PriorityHigh)
                                            )
                                        }
                                        if (hasEvent) {
                                            Box(
                                                modifier = Modifier
                                                    .size(5.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else ElectricCyan)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Events on Selected Date
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal pada $selectedDate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${dayEvents.size} Acara",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (dayEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🗓️", fontSize = 32.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tidak ada jadwal pada tanggal ini",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tekan tombol + di bawah untuk menjadwalkan agenda",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(dayEvents, key = { it.id }) { event ->
                    ScheduleEventCard(event = event)
                }
            }
        }

        // FAB Add Schedule Event
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_schedule_fab"),
            containerColor = NeonIndigo,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Jadwal")
        }

        if (showAddEventDialog) {
            AddEventDialog(
                defaultDate = selectedDate,
                onDismiss = { showAddEventDialog = false },
                onAdd = { title, date, start, end, loc ->
                    viewModel.addScheduleEvent(title, date, start, end, loc)
                    showAddEventDialog = false
                }
            )
        }
    }
}

@Composable
fun ScheduleEventCard(event: ScheduleEvent) {
    val cardColor = if (event.isHoliday) PriorityHigh else Color(event.colorHex)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("event_item_${event.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(cardColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (event.isHoliday) {
                        StatusPill(text = "HARI LIBUR NASIONAL", color = PriorityHigh)
                    } else {
                        StatusPill(text = event.syncSource, color = ElectricCyan)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!event.isHoliday) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Time",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${event.startTime} - ${event.endTime}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (event.location.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Location",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = event.location,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEventDialog(
    defaultDate: String,
    onDismiss: () -> Unit,
    onAdd: (title: String, date: String, start: String, end: String, loc: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(defaultDate) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var location by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jadwal Acara") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Acara") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Mulai") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("Selesai") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Lokasi / Tautan Meeting") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, date, startTime, endTime, location)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Simpan Jadwal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

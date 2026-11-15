package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleEvent
import com.example.data.repository.CalendarDateHelper
import com.example.data.repository.CalendarViewMode
import com.example.data.repository.HolidayProvider
import com.example.data.repository.MonthCalendarData
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MyEvoViewModel
import java.text.SimpleDateFormat
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

    var viewMode by remember { mutableStateOf(CalendarViewMode.ONE_MONTH) }
    var showCountryMenu by remember { mutableStateOf(false) }
    var showAddEventDialog by remember { mutableStateOf(false) }

    val todayStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    val periodLabel = remember(selectedDate, viewMode) {
        CalendarDateHelper.getPeriodHeaderLabel(selectedDate, viewMode)
    }

    val weekDays = remember(selectedDate) {
        CalendarDateHelper.generateWeekDays(selectedDate)
    }

    val monthList = remember(selectedDate, viewMode) {
        when (viewMode) {
            CalendarViewMode.ONE_MONTH -> CalendarDateHelper.generateMultiMonthData(selectedDate, 1)
            CalendarViewMode.THREE_MONTHS -> CalendarDateHelper.generateMultiMonthData(selectedDate, 3)
            CalendarViewMode.SIX_MONTHS -> CalendarDateHelper.generateMultiMonthData(selectedDate, 6)
            CalendarViewMode.ONE_YEAR -> CalendarDateHelper.generateMultiMonthData(selectedDate, 12)
            else -> emptyList()
        }
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
                    onGuestBannerClick = { viewModel.navigateTo(AppScreen.PROFILE) }
                )
            }

            // Country Selector & Holiday Auto-Sync Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    contentDescription = "Negara",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Hari Libur Nasional (${holidaysCount}):",
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
                                            text = { Text("${country.flagEmoji} ${country.name} (${country.code})") },
                                            onClick = {
                                                viewModel.setCountry(country.code, country.name)
                                                showCountryMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = { viewModel.syncGoogleCalendar() },
                                modifier = Modifier.testTag("sync_google_cal_btn")
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonIndigo)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google Cal Sync", fontSize = 11.sp, color = NeonIndigo, fontWeight = FontWeight.Bold)
                            }
                            TextButton(
                                onClick = {
                                    viewModel.setCountry(userProfile.countryCode, userProfile.selectedCountry.substringBefore(" ("))
                                },
                                modifier = Modifier.testTag("btn_sync_holiday_api")
                            ) {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp), tint = ElectricCyan)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sinkron Libur API", fontSize = 11.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Period Display Mode Selector (1 Hari, 1 Minggu, 1 Bulan, 3 Bulan, 6 Bulan, 1 Tahun)
            item {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    Text(
                        text = "Tampilan Rentang Kalender:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CalendarViewMode.entries) { mode ->
                            FilterChip(
                                selected = viewMode == mode,
                                onClick = { viewMode = mode },
                                label = { Text(mode.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonIndigo,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("cal_mode_${mode.name}")
                            )
                        }
                    }
                }
            }

            // Period Navigation Header (< Label > + Hari Ini)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                viewModel.setSelectedCalendarDate(
                                    CalendarDateHelper.shiftAnchorDate(selectedDate, viewMode, -1)
                                )
                            },
                            modifier = Modifier.size(48.dp).testTag("cal_prev_period_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Periode sebelumnya")
                        }
                        Text(
                            text = periodLabel,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = {
                                viewModel.setSelectedCalendarDate(
                                    CalendarDateHelper.shiftAnchorDate(selectedDate, viewMode, 1)
                                )
                            },
                            modifier = Modifier.size(48.dp).testTag("cal_next_period_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Periode berikutnya")
                        }
                    }

                    TextButton(
                        onClick = { viewModel.setSelectedCalendarDate(todayStr) },
                        modifier = Modifier.testTag("cal_today_btn")
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp), tint = NeonIndigo)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hari Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonIndigo)
                    }
                }
            }

            // Dynamic Calendar Content Based on ViewMode
            when (viewMode) {
                CalendarViewMode.ONE_DAY -> {
                    item {
                        SingleDaySummaryBanner(
                            selectedDate = selectedDate,
                            dayEvents = dayEvents,
                            isToday = selectedDate == todayStr
                        )
                    }
                }

                CalendarViewMode.ONE_WEEK -> {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weekDays.forEach { cell ->
                                val isSel = cell.dateStr == selectedDate
                                val hasHoliday = events.any { it.date == cell.dateStr && it.isHoliday }
                                val hasEvent = events.any { it.date == cell.dateStr && !it.isHoliday }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSel) NeonIndigo else MaterialTheme.colorScheme.surface,
                                    border = if (hasHoliday && !isSel) BorderStroke(1.dp, PriorityHigh) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(74.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { viewModel.setSelectedCalendarDate(cell.dateStr) }
                                        .testTag("week_day_${cell.dateStr}")
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = cell.dayOfWeekShort,
                                            fontSize = 10.sp,
                                            color = if (isSel) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = cell.dayOfMonth.toString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else if (hasHoliday) PriorityHigh else MaterialTheme.colorScheme.onSurface
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                            if (hasHoliday) {
                                                Box(
                                                    modifier = Modifier.size(5.dp).clip(CircleShape)
                                                        .background(if (isSel) Color.White else PriorityHigh)
                                                )
                                            }
                                            if (hasEvent) {
                                                Box(
                                                    modifier = Modifier.size(5.dp).clip(CircleShape)
                                                        .background(if (isSel) Color.White else ElectricCyan)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                CalendarViewMode.ONE_MONTH -> {
                    items(monthList) { monthData ->
                        MonthGridCard(
                            monthData = monthData,
                            selectedDate = selectedDate,
                            todayStr = todayStr,
                            events = events,
                            compact = false,
                            onSelectDate = { viewModel.setSelectedCalendarDate(it) }
                        )
                    }
                }

                CalendarViewMode.THREE_MONTHS,
                CalendarViewMode.SIX_MONTHS,
                CalendarViewMode.ONE_YEAR -> {
                    items(monthList.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { monthData ->
                                Box(modifier = Modifier.weight(1f)) {
                                    MonthGridCard(
                                        monthData = monthData,
                                        selectedDate = selectedDate,
                                        todayStr = todayStr,
                                        events = events,
                                        compact = true,
                                        onSelectDate = { viewModel.setSelectedCalendarDate(it) }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Events on Selected Date
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agenda pada $selectedDate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusPill(text = "${dayEvents.size} Acara", color = NeonIndigo)
                }
            }

            if (dayEvents.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🗓️", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tidak ada jadwal pada tanggal ini",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Ketuk tombol + untuk menambah acara",
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
private fun SingleDaySummaryBanner(
    selectedDate: String,
    dayEvents: List<ScheduleEvent>,
    isToday: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("single_day_focus_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NeonIndigo.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isToday) "Fokus Hari Ini ($selectedDate)" else "Fokus Harian ($selectedDate)",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = NeonIndigo
                )
                Text(
                    text = "${dayEvents.size} agenda / hari libur tercatat untuk tanggal ini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(text = "1 Hari", color = EmeraldGreen)
        }
    }
}

@Composable
private fun MonthGridCard(
    monthData: MonthCalendarData,
    selectedDate: String,
    todayStr: String,
    events: List<ScheduleEvent>,
    compact: Boolean,
    onSelectDate: (String) -> Unit
) {
    val weekHeaders = remember { CalendarDateHelper.mondayFirstHeaderLabels() }
    val totalCells = monthData.leadingEmptySlots + monthData.days.size
    val rowsCount = (totalCells + 6) / 7

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!compact) Modifier.padding(horizontal = 16.dp, vertical = 4.dp) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(if (compact) 10.dp else 14.dp)) {
            Text(
                text = monthData.monthTitle,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 12.sp else 14.sp,
                color = NeonIndigo,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth()) {
                weekHeaders.forEach { h ->
                    Text(
                        text = if (compact) h.take(1) else h,
                        fontSize = if (compact) 9.sp else 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            for (rowIdx in 0 until rowsCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (colIdx in 0 until 7) {
                        val slot = rowIdx * 7 + colIdx
                        val dayIndex = slot - monthData.leadingEmptySlots
                        if (dayIndex in monthData.days.indices) {
                            val cell = monthData.days[dayIndex]
                            val isSelected = cell.dateStr == selectedDate
                            val isToday = cell.dateStr == todayStr
                            val hasHoliday = events.any { it.date == cell.dateStr && it.isHoliday }
                            val hasEvent = events.any { it.date == cell.dateStr && !it.isHoliday }

                            Surface(
                                shape = RoundedCornerShape(if (compact) 6.dp else 10.dp),
                                color = when {
                                    isSelected -> NeonIndigo
                                    isToday -> ElectricCyan.copy(alpha = 0.16f)
                                    else -> Color.Transparent
                                },
                                border = if (hasHoliday && !isSelected) BorderStroke(1.dp, PriorityHigh.copy(alpha = 0.7f)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (compact) 26.dp else 40.dp)
                                    .clip(RoundedCornerShape(if (compact) 6.dp else 10.dp))
                                    .clickable { onSelectDate(cell.dateStr) }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = cell.dayOfMonth.toString(),
                                        fontSize = if (compact) 10.sp else 13.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            hasHoliday -> PriorityHigh
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (!compact && (hasHoliday || hasEvent)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            if (hasHoliday) {
                                                Box(
                                                    modifier = Modifier.size(4.dp).clip(CircleShape)
                                                        .background(if (isSelected) Color.White else PriorityHigh)
                                                )
                                            }
                                            if (hasEvent) {
                                                Box(
                                                    modifier = Modifier.size(4.dp).clip(CircleShape)
                                                        .background(if (isSelected) Color.White else ElectricCyan)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
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
                                contentDescription = "Waktu",
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
                                    contentDescription = "Lokasi",
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
                    modifier = Modifier.fillMaxWidth().testTag("event_title_input")
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
                onClick = { if (title.isNotBlank()) onAdd(title, date, startTime, endTime, location) },
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

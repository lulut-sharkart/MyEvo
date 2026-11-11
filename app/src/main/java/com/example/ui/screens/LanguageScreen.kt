package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.DictionaryWord
import com.example.data.model.LanguageLearningEntity
import com.example.data.repository.LanguageLesson
import com.example.data.repository.LanguageLessonProvider
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.LanguageLearningViewModel
import com.example.viewmodel.MyEvoViewModel

@Composable
fun LanguageScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedLesson by viewModel.selectedLesson.collectAsState()
    val crosswordAnswers by viewModel.crosswordAnswers.collectAsState()
    val isCrosswordCompleted by viewModel.isCrosswordCompleted.collectAsState()
    val dictQuery by viewModel.dictionaryQuery.collectAsState()
    val selectedDictLang by viewModel.selectedDictLanguage.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Modul Belajar AI, 1: Kamus Offline, 2: Teka-Teki Silang Harian

    val context = LocalContext.current
    val langLearningVm: LanguageLearningViewModel = viewModel(
        factory = LanguageLearningViewModel.provideFactory(context.applicationContext as android.app.Application)
    )
    val vocabList by langLearningVm.filteredVocabulary.collectAsState()
    val totalWords by langLearningVm.totalWordCount.collectAsState()
    val masteredWords by langLearningVm.masteredCount.collectAsState()
    val inProgressWords by langLearningVm.inProgressCount.collectAsState()
    val masteryRate by langLearningVm.overallMasteryRate.collectAsState()
    val selectedLang by langLearningVm.selectedLanguage.collectAsState()
    val vocabSearchQuery by langLearningVm.searchQuery.collectAsState()

    var showAddVocabDialog by remember { mutableStateOf(false) }

    // Fallback static dictionary words filtered
    val wordsList = remember(dictQuery, selectedDictLang) {
        var list = LanguageLessonProvider.initialDictionaryWords
        if (selectedDictLang != "All") {
            list = list.filter { it.language.equals(selectedDictLang, ignoreCase = true) }
        }
        if (dictQuery.isNotBlank()) {
            list = list.filter {
                it.word.contains(dictQuery, ignoreCase = true) ||
                    it.translation.contains(dictQuery, ignoreCase = true)
            }
        }
        list
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Belajar Bahasa & Teka-Teki",
                    subtitle = if (userProfile.isGuest) "Mode Tamu • Latihan AI Offline" else "Latihan AI interaktif, kamus offline & TTS",
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

            // Tabs: Modul AI vs Kamus Offline vs Teka-Teki Silang
            item {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonIndigo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Modul AI", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_modules")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Kamus Offline", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_dict")
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Teka-Teki Silang", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_crossword")
                    )
                }
            }

            if (activeTab == 0) {
                // TAB 0: MODUL BELAJAR BAHASA AI
                if (selectedLesson == null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🤖 Modul Latihan Interaktif Berbasis AI",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Asah kemampuan berbicara, kosakata, dan tata bahasa dengan koreksi pintar & panduan pelafalan.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Pilih Modul Pembelajaran:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(viewModel.languageLessons, key = { it.id }) { lesson ->
                        LessonCard(
                            lesson = lesson,
                            onStart = { viewModel.selectLesson(lesson) }
                        )
                    }
                } else {
                    // Active Interactive Quiz Session
                    item {
                        ActiveLessonQuizView(
                            lesson = selectedLesson!!,
                            onBack = { viewModel.selectLesson(null) }
                        )
                    }
                }
            } else if (activeTab == 1) {
                // TAB 1: KOSAKATA & KAMUS OFFLINE DENGAN PROGRESS TRACKING
                item {
                    // Progress Tracking Overview Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("vocab_progress_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "📈 Progres Belajar Kosakata",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Total $totalWords kata • $masteredWords dikuasai",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(
                                    text = "${(masteryRate * 100).toInt()}% Selesai",
                                    color = if (masteryRate >= 0.7f) EmeraldGreen else NeonIndigo
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { masteryRate },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonIndigo,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Column {
                                        Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$totalWords", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Dikuasai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$masteredWords", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldGreen)
                                    }
                                    Column {
                                        Text("Diproses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$inProgressWords", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CoralOrange)
                                    }
                                }

                                Button(
                                    onClick = { showAddVocabDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("btn_add_vocab")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Tambah Kata", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = vocabSearchQuery,
                            onValueChange = { langLearningVm.setSearchQuery(it) },
                            placeholder = { Text("Cari kosakata, arti, atau contoh...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = NeonIndigo)
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dict_search_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Language filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val langs = listOf("All", "English", "Japanese", "Korean", "German")
                            items(langs) { lang ->
                                FilterChip(
                                    selected = selectedLang == lang,
                                    onClick = { langLearningVm.setSelectedLanguage(lang) },
                                    label = { Text(lang, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kosakata Tersimpan (${vocabList.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(vocabList, key = { it.id }) { vocab ->
                    LanguageLearningWordCard(
                        vocab = vocab,
                        onRecordReview = { remembered ->
                            langLearningVm.recordReview(vocab.id, remembered)
                        },
                        onToggleMastered = {
                            langLearningVm.toggleMastered(vocab.id)
                        },
                        onDelete = {
                            langLearningVm.deleteVocabulary(vocab)
                        }
                    )
                }
            } else {
                // TAB 2: TEKA-TEKI SILANG HARIAN (DAILY CROSSWORD)
                val puzzle = viewModel.dailyCrossword
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("crossword_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = puzzle.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Topik Hari Ini: ${puzzle.topic}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                StatusPill(text = "+${puzzle.xpReward} XP", color = AmberGold)
                            }

                            // Visual 7x7 Crossword Grid
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Papan Kotak Teka-Teki Silang (7x7):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp)
                                    .testTag("crossword_grid_7x7"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                for (r in 0 until puzzle.gridSize) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        for (c in 0 until puzzle.gridSize) {
                                            var cellChar: Char? = null
                                            var cellNumber: Int? = null
                                            var isActiveCell = false

                                            for (clue in puzzle.clues) {
                                                val userAns = crosswordAnswers[clue.number] ?: ""
                                                if (clue.direction == "ACROSS") {
                                                    if (r == clue.startRow && c >= clue.startCol && c < clue.startCol + clue.answer.length) {
                                                        isActiveCell = true
                                                        val offset = c - clue.startCol
                                                        if (r == clue.startRow && c == clue.startCol) {
                                                            cellNumber = clue.number
                                                        }
                                                        if (offset < userAns.length) {
                                                            cellChar = userAns[offset].uppercaseChar()
                                                        }
                                                    }
                                                } else if (clue.direction == "DOWN") {
                                                    if (c == clue.startCol && r >= clue.startRow && r < clue.startRow + clue.answer.length) {
                                                        isActiveCell = true
                                                        val offset = r - clue.startRow
                                                        if (r == clue.startRow && c == clue.startCol) {
                                                            cellNumber = clue.number
                                                        }
                                                        if (offset < userAns.length) {
                                                            cellChar = userAns[offset].uppercaseChar()
                                                        }
                                                    }
                                                }
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (isActiveCell) {
                                                            if (isCrosswordCompleted) EmeraldGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                                        } else {
                                                            Color(0xFF1E293B).copy(alpha = 0.25f)
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isActiveCell) 1.dp else 0.5.dp,
                                                        color = if (isActiveCell) NeonIndigo.copy(alpha = 0.6f) else Color.Transparent,
                                                        shape = RoundedCornerShape(6.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (cellNumber != null) {
                                                    Text(
                                                        text = cellNumber.toString(),
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = NeonIndigo,
                                                        modifier = Modifier
                                                            .align(Alignment.TopStart)
                                                            .padding(2.dp)
                                                    )
                                                }
                                                if (cellChar != null) {
                                                    Text(
                                                        text = cellChar.toString(),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp,
                                                        color = if (isCrosswordCompleted) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (isCrosswordCompleted) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🏆", fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "SELESAI DENGAN SEMPURNA!",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.sp,
                                                color = EmeraldGreen
                                            )
                                            Text(
                                                text = "Jawaban teka-teki silang Anda 100% benar. XP telah ditambahkan ke profil Anda!",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Petunjuk & Isian Huruf:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(puzzle.clues, key = { it.number }) { clue ->
                    val currentVal = crosswordAnswers[clue.number] ?: ""
                    val isCorrect = currentVal.equals(clue.answer, ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCorrect) EmeraldGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(NeonIndigo),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = clue.number.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = clue.direction,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricCyan
                                    )
                                }

                                if (isCorrect) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("Benar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = clue.clue,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = currentVal,
                                    onValueChange = { viewModel.updateCrosswordAnswer(clue.number, it) },
                                    label = { Text("Jawaban (${clue.answer.length} huruf)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("crossword_input_${clue.number}")
                                )

                                // Reveal Hint Button
                                IconButton(
                                    onClick = {
                                        viewModel.updateCrosswordAnswer(clue.number, clue.answer.take(1) + currentVal.drop(1))
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Lightbulb, contentDescription = "Hint", tint = AmberGold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddVocabDialog) {
            var wordInput by remember { mutableStateOf("") }
            var transInput by remember { mutableStateOf("") }
            var langInput by remember { mutableStateOf(if (selectedLang != "All") selectedLang else "English") }
            var phoneticInput by remember { mutableStateOf("") }
            var exampleInput by remember { mutableStateOf("") }
            var exampleTransInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddVocabDialog = false },
                title = {
                    Text(
                        text = "Tambah Kosakata Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wordInput,
                            onValueChange = { wordInput = it },
                            label = { Text("Kata / Frasa *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_vocab_word")
                        )
                        OutlinedTextField(
                            value = transInput,
                            onValueChange = { transInput = it },
                            label = { Text("Arti Terjemahan *") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_vocab_trans")
                        )
                        Text("Bahasa:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val availableLangs = listOf("English", "Japanese", "Korean", "German")
                            items(availableLangs) { lang ->
                                FilterChip(
                                    selected = langInput == lang,
                                    onClick = { langInput = lang },
                                    label = { Text(lang, fontSize = 10.sp) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = phoneticInput,
                            onValueChange = { phoneticInput = it },
                            label = { Text("Pelafalan / Fonetik (opsional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = exampleInput,
                            onValueChange = { exampleInput = it },
                            label = { Text("Contoh Kalimat (opsional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = exampleTransInput,
                            onValueChange = { exampleTransInput = it },
                            label = { Text("Terjemahan Contoh (opsional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (wordInput.isNotBlank() && transInput.isNotBlank()) {
                                langLearningVm.addVocabulary(
                                    word = wordInput,
                                    translation = transInput,
                                    language = langInput,
                                    phonetic = phoneticInput,
                                    exampleSentence = exampleInput,
                                    exampleTranslation = exampleTransInput
                                )
                                showAddVocabDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier.testTag("btn_confirm_add_vocab")
                    ) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddVocabDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun LessonCard(
    lesson: LanguageLesson,
    onStart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("lesson_card_${lesson.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = lesson.iconEmoji, fontSize = 34.sp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusPill(text = lesson.language, color = NeonIndigo)
                    StatusPill(text = lesson.level, color = ElectricCyan)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = lesson.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onStart,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                contentPadding = PaddingValues(10.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = Color.White)
            }
        }
    }
}

@Composable
fun ActiveLessonQuizView(
    lesson: LanguageLesson,
    onBack: () -> Unit
) {
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var hasAnswered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }

    val question = lesson.questions[currentQuestionIdx]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Pertanyaan ${currentQuestionIdx + 1} dari ${lesson.questions.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusPill(text = lesson.language, color = NeonIndigo)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = question.prompt,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"${question.phrase}\"",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeonIndigo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Audio TTS", tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = question.audioHint,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Options
            question.options.forEachIndexed { idx, option ->
                val isSelected = selectedOptionIdx == idx
                val isCorrect = idx == question.correctIndex

                val btnBg = when {
                    hasAnswered && isCorrect -> EmeraldGreen.copy(alpha = 0.2f)
                    hasAnswered && isSelected && !isCorrect -> PriorityHigh.copy(alpha = 0.2f)
                    isSelected -> NeonIndigo.copy(alpha = 0.15f)
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = btnBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!hasAnswered) {
                                selectedOptionIdx = idx
                                hasAnswered = true
                                showExplanation = true
                            }
                        }
                        .testTag("quiz_option_$idx")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) NeonIndigo else Color.LightGray.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ('A' + idx).toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Explanation Card
            if (showExplanation) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selectedOptionIdx == question.correctIndex) EmeraldGreen.copy(alpha = 0.12f) else CoralOrange.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (selectedOptionIdx == question.correctIndex) "🎉 Jawaban Benar!" else "💡 Pembahasan AI:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (selectedOptionIdx == question.correctIndex) EmeraldGreen else CoralOrange
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = question.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (currentQuestionIdx < lesson.questions.size - 1) {
                            currentQuestionIdx++
                            selectedOptionIdx = null
                            hasAnswered = false
                            showExplanation = false
                        } else {
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (currentQuestionIdx < lesson.questions.size - 1) "Pertanyaan Berikutnya ➔" else "Selesaikan Modul & Ambil XP"
                    )
                }
            }
        }
    }
}

@Composable
fun DictionaryWordCard(word: DictionaryWord) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = word.word,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = word.pronunciation,
                        fontSize = 11.sp,
                        color = ElectricCyan
                    )
                }
                StatusPill(text = word.language, color = NeonIndigo)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Arti: ${word.translation}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = CoralOrange
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Contoh: \"${word.exampleSentence}\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LanguageLearningWordCard(
    vocab: LanguageLearningEntity,
    onRecordReview: (Boolean) -> Unit,
    onToggleMastered: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("vocab_card_${vocab.id}"),
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
                    Text(
                        text = vocab.word,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (vocab.phonetic.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = vocab.phonetic,
                            fontSize = 11.sp,
                            color = ElectricCyan
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(text = vocab.language, color = NeonIndigo)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onToggleMastered,
                        modifier = Modifier.size(28.dp).testTag("btn_master_star_${vocab.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = if (vocab.isMastered) "Dikuasai" else "Belum Dikuasai",
                            tint = if (vocab.isMastered || vocab.masteryLevel >= 5) AmberGold else Color.LightGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Arti: ${vocab.translation}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = CoralOrange
            )

            if (vocab.exampleSentence.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Contoh: \"${vocab.exampleSentence}\"",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (vocab.exampleTranslation.isNotBlank()) {
                Text(
                    text = "(${vocab.exampleTranslation})",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level Stars & Review Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mastery stars (0 to 5)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (i <= vocab.masteryLevel) AmberGold else Color.LightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lvl ${vocab.masteryLevel}/5 (${vocab.reviewCount}x uji)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Review Actions
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = { onRecordReview(false) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("btn_review_miss_${vocab.id}")
                    ) {
                        Text("Lupa", fontSize = 10.sp, color = PriorityHigh)
                    }
                    Button(
                        onClick = { onRecordReview(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp).testTag("btn_review_hit_${vocab.id}")
                    ) {
                        Text("Ingat", fontSize = 10.sp, color = Color.White)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("btn_delete_vocab_${vocab.id}")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Hapus",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

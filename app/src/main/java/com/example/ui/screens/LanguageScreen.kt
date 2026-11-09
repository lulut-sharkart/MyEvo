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
import com.example.data.model.DictionaryWord
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

    // Dictionary words filtered
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
                // TAB 1: KAMUS DIGITAL OFFLINE
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = dictQuery,
                            onValueChange = { viewModel.setDictionaryQuery(it) },
                            placeholder = { Text("Cari kosakata atau arti terjemahan...") },
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
                                    selected = selectedDictLang == lang,
                                    onClick = { viewModel.setSelectedDictLanguage(lang) },
                                    label = { Text(lang, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Kosakata Offline Tersimpan (${wordsList.size}):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                items(wordsList, key = { it.word + it.language }) { wordItem ->
                    DictionaryWordCard(word = wordItem)
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

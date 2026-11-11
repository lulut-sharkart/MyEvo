package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiGalleryItem
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.SunsetPink
import com.example.viewmodel.AiBackgroundStyle
import com.example.viewmodel.MyEvoViewModel

@Composable
fun AIPhotoStudioScreen(
    viewModel: MyEvoViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware and gesture back
    BackHandler { onBack() }

    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val selectedBg by viewModel.selectedAiBackground.collectAsState()
    val isProcessing by viewModel.isAiProcessing.collectAsState()
    val isProcessed by viewModel.backgroundRemovalSuccess.collectAsState()

    val aiGallery by viewModel.aiGallery.collectAsState()
    val isGeneratingPhoto by viewModel.isGeneratingPhoto.collectAsState()
    val generatedResult by viewModel.generatedPhotoResult.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Transformasi AI, 1: Kreasi Foto Baru, 2: Galeri Karya
    var showBeforeView by remember { mutableStateOf(false) }
    var selectedPresetSubject by remember { mutableStateOf("Profesional Portrait") }
    var customTransformPrompt by remember { mutableStateOf("") }
    var userCreativePrompt by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk Neon") }
    var selectedAspectRatio by remember { mutableStateOf("1:1") }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var galleryFilter by remember { mutableStateOf("Semua") } // "Semua", "Transformasi", "Kreasi Baru"

    val presetSubjects = listOf("Profesional Portrait", "Produk Sepatu Sneaker", "Kopi & Buku Estetik", "Kucing Gemas", "Pemandangan Alam")

    val filteredGallery = remember(aiGallery, galleryFilter) {
        when (galleryFilter) {
            "Transformasi" -> aiGallery.filter { it.isTransformation }
            "Kreasi Baru" -> aiGallery.filter { !it.isTransformation }
            else -> aiGallery
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header with Back Button
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
                                .testTag("btn_back_studio")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = NeonIndigo
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AI Photo Studio",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusPill(text = "Gemini AI", color = ElectricCyan)
                            }
                            Text(
                                text = "Transformasi latar, sintesis foto & galeri karya pintar",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { activeTab = 2 },
                            modifier = Modifier.testTag("btn_top_gallery")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeri", tint = NeonIndigo)
                        }
                    }
                }
            }

            // Notification / Feedback Banner
            if (feedbackMessage != null) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = feedbackMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldGreen,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { feedbackMessage = null }) {
                                Text("Tutup", fontSize = 11.sp, color = EmeraldGreen)
                            }
                        }
                    }
                }
            }

            // 3-Way Tab Selector: Transformasi vs Kreasi Baru vs Galeri
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
                        text = { Text("Transformasi AI", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_transform")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Kreasi Foto Baru", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_create")
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Galeri AI (${aiGallery.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        modifier = Modifier.testTag("tab_gallery")
                    )
                }
            }

            if (activeTab == 0) {
                // ==========================================
                // TAB 0: TRANSFORMASI FOTO DENGAN GEMINI AI
                // ==========================================
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            text = "1. Pilih Objek Foto Subjek:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(presetSubjects) { subject ->
                                val isSel = selectedPresetSubject == subject
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) NeonIndigo else MaterialTheme.colorScheme.surface,
                                    border = if (!isSel) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedPresetSubject = subject }
                                ) {
                                    Text(
                                        text = subject,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Canvas Preview & Comparison
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(250.dp)
                            .testTag("ai_canvas_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    if (showBeforeView || !isProcessed) {
                                        Brush.linearGradient(listOf(Color(0xFF64748B), Color(0xFF334155)))
                                    } else {
                                        when (selectedBg) {
                                            AiBackgroundStyle.NEON_STUDIO -> Brush.radialGradient(listOf(Color(0xFF8B5CF6), Color(0xFF0F172A)))
                                            AiBackgroundStyle.COZY_CAFE -> Brush.linearGradient(listOf(Color(0xFF78350F), Color(0xFF451A03)))
                                            AiBackgroundStyle.SUNSET_NATURE -> Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFBE185D)))
                                            AiBackgroundStyle.TRANSPARENT_CLEAN -> Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1)))
                                            AiBackgroundStyle.MINIMAL_PASTEL -> Brush.horizontalGradient(listOf(Color(0xFFC084FC), Color(0xFF67E8F9)))
                                        }
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = when (selectedPresetSubject) {
                                        "Profesional Portrait" -> "👤"
                                        "Produk Sepatu Sneaker" -> "👟"
                                        "Kopi & Buku Estetik" -> "☕"
                                        "Pemandangan Alam" -> "🏔️"
                                        else -> "🐱"
                                    },
                                    fontSize = 68.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.Black.copy(alpha = 0.5f)
                                ) {
                                    Text(
                                        text = selectedPresetSubject,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Before / After Indicator Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = if (showBeforeView || !isProcessed) "FOTO ASLI (BEFORE)" else "HASIL GEMINI AI (${selectedBg.label})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Compare Button
                            if (isProcessed) {
                                Button(
                                    onClick = { showBeforeView = !showBeforeView },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(12.dp)
                                        .testTag("btn_toggle_compare")
                                ) {
                                    Icon(Icons.Default.Compare, contentDescription = "Compare", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (showBeforeView) "Lihat Hasil AI" else "Tahan Cek Asli",
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            // Processing Spinner
                            if (isProcessing) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.65f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = ElectricCyan)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Gemini AI Segmenting & Style Transfer...",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Style Presets
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            text = "2. Pilih Gaya & Suasana Latar AI:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(AiBackgroundStyle.values()) { style ->
                                val isSel = selectedBg == style
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) Color(style.colorCode) else MaterialTheme.colorScheme.surface,
                                    border = if (!isSel) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant) else null,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.setAiBackground(style) }
                                ) {
                                    Text(
                                        text = style.label,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Custom Prompt Input
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        OutlinedTextField(
                            value = customTransformPrompt,
                            onValueChange = { customTransformPrompt = it },
                            label = { Text("Instruksi Tambahan Gemini AI (opsional)") },
                            placeholder = { Text("Contoh: Berikan pencahayaan hangat rembrandt & partikel halus...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("input_custom_transform_prompt")
                        )
                    }
                }

                // Action Buttons: Transform & Save to Gallery
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.processAiBackgroundRemoval() },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_transform_photo")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Transformasi AI")
                        }

                        if (isProcessed) {
                            Button(
                                onClick = {
                                    viewModel.saveToAiGallery(
                                        title = "$selectedPresetSubject - ${selectedBg.label}",
                                        prompt = customTransformPrompt.ifBlank { selectedBg.promptDescription },
                                        style = selectedBg.label,
                                        subject = selectedPresetSubject,
                                        isTransformation = true,
                                        colorPrimary = selectedBg.colorCode,
                                        colorSecondary = 0xFF6366F1
                                    )
                                    feedbackMessage = "Foto transformasi berhasil disimpan ke Galeri AI!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_save_transformed_gallery")
                            ) {
                                Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Simpan Galeri")
                            }
                        }
                    }
                }
            } else if (activeTab == 1) {
                // ==========================================
                // TAB 1: KREASI FOTO BARU (TEXT-TO-IMAGE)
                // ==========================================
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        Text(
                            text = "🎨 Deskripsikan Foto yang Ingin Kamu Buat:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = userCreativePrompt,
                            onValueChange = { userCreativePrompt = it },
                            placeholder = { Text("Contoh: Kucing astronot mengambang di luar angkasa dengan latar bumi berpendar biru...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .testTag("input_creative_prompt")
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Inspirasi Prompt Cepat:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val inspirations = listOf(
                                "Avatar 3D Robot Cyberpunk",
                                "Gunung Bromo Fajar Sinematik 8K",
                                "Kamar Belajar Estetik Hujan Tokyo",
                                "Mobil Konsep Minimalis Futuristik"
                            )
                            items(inspirations) { insp ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { userCreativePrompt = insp }
                                ) {
                                    Text(
                                        text = insp,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gaya Visual
                        Text("Pilih Gaya Artistik:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val styles = listOf("Cyberpunk Neon", "Anime Manga", "Golden Hour", "Minimalist 3D", "Cinematic Film")
                            items(styles) { st ->
                                FilterChip(
                                    selected = selectedStyle == st,
                                    onClick = { selectedStyle = st },
                                    label = { Text(st, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Aspek Rasio
                        Text("Rasio Foto:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("1:1 (Persegi)", "16:9 (Landscape)", "9:16 (Portrait)").forEach { ratioLabel ->
                                val code = ratioLabel.substringBefore(" ")
                                FilterChip(
                                    selected = selectedAspectRatio == code,
                                    onClick = { selectedAspectRatio = code },
                                    label = { Text(ratioLabel, fontSize = 10.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (userCreativePrompt.isNotBlank()) {
                                    viewModel.generatePhotoWithGemini(userCreativePrompt, selectedStyle, selectedAspectRatio)
                                }
                            },
                            enabled = userCreativePrompt.isNotBlank() && !isGeneratingPhoto,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_generate_photo")
                        ) {
                            if (isGeneratingPhoto) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Gemini AI Sedang Mensintesis...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hasilkan Foto Baru (Gemini AI)")
                            }
                        }
                    }
                }

                // Generated Preview Box
                if (generatedResult != null) {
                    val res = generatedResult!!
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("generated_preview_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("✨ Hasil Kreasi Gemini AI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    StatusPill(text = res.style, color = NeonIndigo)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.radialGradient(
                                                listOf(Color(res.colorPrimary), Color(res.colorSecondary))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🎨", fontSize = 54.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.6f)
                                        ) {
                                            Text(
                                                text = res.title,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "\"${res.prompt}\"",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.saveToAiGallery(
                                            title = res.title,
                                            prompt = res.prompt,
                                            style = res.style,
                                            subject = res.subject,
                                            isTransformation = false,
                                            ratio = res.ratio,
                                            colorPrimary = res.colorPrimary,
                                            colorSecondary = res.colorSecondary
                                        )
                                        feedbackMessage = "Karya foto baru berhasil disimpan ke Galeri!"
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_save_generated_gallery")
                                ) {
                                    Icon(Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simpan ke Galeri AI")
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // TAB 2: GALERI KARYA FOTO TERSIMPAN
                // ==========================================
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Koleksi Karya (${aiGallery.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Semua", "Transformasi", "Kreasi Baru").forEach { filter ->
                                FilterChip(
                                    selected = galleryFilter == filter,
                                    onClick = { galleryFilter = filter },
                                    label = { Text(filter, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                if (filteredGallery.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🖼️", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Belum Ada Foto Tersimpan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Buat karya pertamamu di tab Transformasi AI atau Kreasi Baru!",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { activeTab = 0 },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                                ) {
                                    Text("Mulai Transformasi")
                                }
                            }
                        }
                    }
                } else {
                    items(filteredGallery, key = { it.id }) { item ->
                        GalleryItemCard(
                            item = item,
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Lihat karya foto saya yang dibuat dengan MyEvo AI Studio: \"${item.title}\" (${item.style})!\n\nPrompt: \"${item.prompt}\"")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Bagikan Foto AI MyEvo"))
                            },
                            onDownload = {
                                feedbackMessage = "\"${item.title}\" berhasil diekspor ke memori penyimpanan lokal!"
                            },
                            onDelete = {
                                viewModel.deleteFromAiGallery(item.id)
                                feedbackMessage = "Foto berhasil dihapus dari galeri."
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryItemCard(
    item: AiGalleryItem,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("gallery_item_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Visual Artwork Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(item.colorPrimary), Color(item.colorSecondary))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (item.isTransformation) "✨" else "🎨",
                        fontSize = 44.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f)
                    ) {
                        Text(
                            text = item.subject,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                StatusPill(
                    text = if (item.isTransformation) "Transformasi" else "Kreasi Baru",
                    color = if (item.isTransformation) NeonIndigo else SunsetPink,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gaya: ${item.style} • ${item.dateStr}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Prompt: \"${item.prompt}\"",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("btn_share_${item.id}")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bagikan", fontSize = 11.sp)
                    }
                    Button(
                        onClick = onDownload,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("btn_download_${item.id}")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor", fontSize = 11.sp)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp).testTag("btn_delete_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = PriorityHigh,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

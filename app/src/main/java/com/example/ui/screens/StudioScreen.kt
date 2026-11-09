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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.SunsetPink
import com.example.viewmodel.AiBackgroundStyle
import com.example.viewmodel.MyEvoViewModel

@Composable
fun StudioScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val selectedBg by viewModel.selectedAiBackground.collectAsState()
    val isProcessing by viewModel.isAiProcessing.collectAsState()
    val isProcessed by viewModel.backgroundRemovalSuccess.collectAsState()

    var showBeforeView by remember { mutableStateOf(false) }
    var selectedPresetSubject by remember { mutableStateOf("Profesional Portrait") }

    val presetSubjects = listOf("Profesional Portrait", "Produk Sepatu Sneaker", "Kopi & Buku Estetik", "Kucing Gemas")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Header
        item {
            AppHeader(
                title = "AI Photo Studio",
                subtitle = if (userProfile.isGuest) "Mode Tamu • Edit Latar AI" else "Edit & hapus latar belakang foto instan",
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

        // Subject Selector
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    text = "Pilih Objek Foto untuk Diedit:",
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

        // Live Photo Canvas & Split Comparison View
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(260.dp)
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
                    // Center Subject Graphic
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when (selectedPresetSubject) {
                                "Profesional Portrait" -> "👤"
                                "Produk Sepatu Sneaker" -> "👟"
                                "Kopi & Buku Estetik" -> "☕"
                                else -> "🐱"
                            },
                            fontSize = 72.sp
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
                            text = if (showBeforeView || !isProcessed) "FOTO ASLI (BEFORE)" else "HASIL AI (${selectedBg.label})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Comparison Toggle Button
                    if (isProcessed) {
                        Button(
                            onClick = { showBeforeView = !showBeforeView },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .testTag("compare_slider_btn")
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
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = ElectricCyan)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Neural AI Segmenting Foreground...",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // AI Background Styles Selector
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Pilih Pengganti Latar Belakang AI:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                AiBackgroundStyle.values().forEach { style ->
                    val isSel = selectedBg == style
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.setAiBackground(style) }
                            .testTag("bg_style_${style.name}"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) NeonIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, NeonIndigo) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(style.colorCode))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = style.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = style.promptDescription,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSel) {
                                StatusPill(text = "Dipilih", color = NeonIndigo)
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.processAiBackgroundRemoval() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("process_ai_bg_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isProcessed) "Ubah Latar Belakang AI Lagi" else "Proses Hapus & Ganti Latar Belakang AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                if (isProcessed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.processAiBackgroundRemoval()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Unduh", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan Foto", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                // Simulate sharing
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Bagikan", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bagikan", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

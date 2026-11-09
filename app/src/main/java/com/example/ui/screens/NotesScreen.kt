package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EncryptedNote
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.PriorityHigh
import com.example.viewmodel.MyEvoViewModel

@Composable
fun NotesScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    val unlockedNotes by viewModel.unlockedNotes.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteToUnlock by remember { mutableStateOf<EncryptedNote?>(null) }
    var unlockPin by remember { mutableStateOf("") }
    var unlockError by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Catatan Harian Terenkripsi",
                    subtitle = if (userProfile.isGuest) "Mode Tamu • Enkripsi Privat Lokal" else "Keamanan privat AES end-to-end",
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

            // Security Badge Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("security_badge_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = EmeraldGreen.copy(alpha = 0.12f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Enkripsi End-to-End Aktif",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Isi catatan dienkripsi dengan PIN Anda menggunakan algoritma AES-256. Tidak ada pihak ketiga yang dapat membaca.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notes list
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buku Harian Rahasia (${notes.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "PIN Default demo: 1234",
                        fontSize = 11.sp,
                        color = AmberGold
                    )
                }
            }

            if (notes.isEmpty()) {
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
                            Text("🔐", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tulis isi pikiran atau rahasiamu dengan perlindungan AES-256.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { showAddNoteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tulis Catatan Rahasia Pertama")
                            }
                        }
                    }
                }
            } else {
                items(notes, key = { it.id }) { note ->
                    val decryptedContent = unlockedNotes[note.id]
                    val isUnlocked = decryptedContent != null

                    EncryptedNoteCard(
                        note = note,
                        decryptedContent = decryptedContent,
                        isUnlocked = isUnlocked,
                        onUnlockClick = {
                            noteToUnlock = note
                            unlockPin = ""
                            unlockError = false
                        },
                        onLockClick = { viewModel.lockNote(note.id) },
                        onDeleteClick = { viewModel.deleteNote(note) }
                    )
                }
            }
        }

        // FAB Add Note
        FloatingActionButton(
            onClick = { showAddNoteDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 20.dp)
                .testTag("add_note_fab"),
            containerColor = NeonIndigo,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, contentDescription = "Tambah Catatan")
        }

        // Add Note Dialog
        if (showAddNoteDialog) {
            AddEncryptedNoteDialog(
                onDismiss = { showAddNoteDialog = false },
                onAdd = { title, content, pin, mood, tag ->
                    viewModel.addEncryptedNote(title, content, pin, mood, tag)
                    showAddNoteDialog = false
                }
            )
        }

        // Unlock PIN Dialog
        if (noteToUnlock != null) {
            AlertDialog(
                onDismissRequest = { noteToUnlock = null },
                title = { Text("Buka Catatan Terenkripsi") },
                text = {
                    Column {
                        Text(
                            text = "Masukkan PIN rahasia 4 digit untuk melihat isi catatan '${noteToUnlock?.title}'.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = unlockPin,
                            onValueChange = {
                                unlockPin = it
                                unlockError = false
                            },
                            label = { Text("PIN Enkripsi") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            isError = unlockError,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_input")
                        )
                        if (unlockError) {
                            Text(
                                text = "PIN salah! Silakan coba lagi (coba 1234).",
                                color = PriorityHigh,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val success = viewModel.unlockNoteWithPin(noteToUnlock!!, unlockPin)
                            if (success) {
                                noteToUnlock = null
                            } else {
                                unlockError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                        modifier = Modifier.testTag("submit_pin_btn")
                    ) {
                        Text("Buka Catatan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noteToUnlock = null }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
fun EncryptedNoteCard(
    note: EncryptedNote,
    decryptedContent: String?,
    isUnlocked: Boolean,
    onUnlockClick: () -> Unit,
    onLockClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("note_item_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = note.mood, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(text = note.tag, color = ElectricCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = if (isUnlocked) onLockClick else onUnlockClick,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("lock_toggle_${note.id}")
                    ) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = if (isUnlocked) "Lock" else "Unlock",
                            tint = if (isUnlocked) EmeraldGreen else AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (isUnlocked && decryptedContent != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = decryptedContent,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onUnlockClick)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🔒 ${note.previewSnippet}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Ketuk untuk Buka PIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonIndigo
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dibuat: ${note.date}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddEncryptedNoteDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, content: String, pin: String, mood: String, tag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var mood by remember { mutableStateOf("😊") }
    var tag by remember { mutableStateOf("Pribadi") }

    val moods = listOf("😊", "🚀", "💡", "🌿", "🧘", "💼")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tulis Catatan Harian Rahasia") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Catatan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Isi Catatan Rahasia") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it },
                    label = { Text("Kunci PIN / Sandi Pengaman") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Mood Hari Ini:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(moods) { m ->
                        val isSel = mood == m
                        Surface(
                            shape = CircleShape,
                            color = if (isSel) NeonIndigo.copy(alpha = 0.2f) else Color.Transparent,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.5.dp, NeonIndigo) else null,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { mood = m }
                                .padding(4.dp)
                        ) {
                            Text(text = m, fontSize = 22.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank() && pin.isNotBlank()) {
                        onAdd(title, content, pin, mood, tag)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
            ) {
                Text("Enkripsi & Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

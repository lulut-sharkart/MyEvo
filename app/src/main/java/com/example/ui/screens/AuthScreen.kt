package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.viewmodel.MyEvoViewModel

@Composable
fun AuthScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showGuestDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Logo
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(NeonIndigo, ElectricCyan, CoralOrange)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_myevo_logo),
                contentDescription = "MyEvo Logo",
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MyEvo",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = "Asisten Cerdas Harian & Produktivitas All-in-One",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Value props badge row
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            StatusPill(text = "⚡ Prioritas Pintar", color = NeonIndigo)
            StatusPill(text = "💰 Anggaran", color = EmeraldGreen)
            StatusPill(text = "🔐 Enkripsi AES", color = AmberGold)
            StatusPill(text = "☁️ Cloud & Offline", color = ElectricCyan)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_card"),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih Metode Masuk:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // 1. Google Sign-In Button
                Button(
                    onClick = { showGoogleDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_google_signin"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEA4335),
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lanjutkan dengan Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 2. Facebook Sign-In Button
                Button(
                    onClick = { showFacebookDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_facebook_signin"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("f", color = Color(0xFF1877F2), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Lanjutkan dengan Facebook",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                // 3. Phone Number Sign-In Button
                Button(
                    onClick = { showPhoneDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_phone_signin"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldGreen,
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Masuk dengan Nomor Telepon (OTP)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = " atau untuk pengujian ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                // 4. Mode Tamu (Guest Mode) Button
                OutlinedButton(
                    onClick = { showGuestDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_guest_mode"),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberGold)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Masuk sebagai Mode Tamu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AmberGold
                            )
                            Text(
                                text = "Tanpa sinkronisasi cloud • Data lokal saja",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dengan masuk, Anda menyetujui privasi end-to-end terenkripsi dan ketentuan penggunaan aplikasi MyEvo.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Google Sign-In Account Selector Dialog
    if (showGoogleDialog) {
        GoogleSignInDialog(
            onDismiss = { showGoogleDialog = false },
            onSelectAccount = { name, email ->
                viewModel.loginWithGoogle(name, email)
                showGoogleDialog = false
            }
        )
    }

    // Facebook Login Dialog
    if (showFacebookDialog) {
        FacebookSignInDialog(
            onDismiss = { showFacebookDialog = false },
            onConfirm = { name, email ->
                viewModel.loginWithFacebook(name, email)
                showFacebookDialog = false
            }
        )
    }

    // Phone Number & OTP Dialog
    if (showPhoneDialog) {
        PhoneAuthDialog(
            onDismiss = { showPhoneDialog = false },
            onVerified = { phone ->
                viewModel.loginWithPhone(phone)
                showPhoneDialog = false
            }
        )
    }

    // Guest Mode Confirmation Dialog
    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Konfirmasi Mode Tamu")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Mode Tamu memungkinkan Anda menguji seluruh fitur aplikasi (jadwal, kalkulator anggaran, teka-teki silang, photo studio, dan catatan enkripsi).",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberGold.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ Perhatian: Data yang Anda masukkan hanya disimpan secara lokal di perangkat ini dan TIDAK akan disinkronkan ke server awan (cloud). Anda dapat beralih ke akun resmi kapan saja melalui menu Profil.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberGold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.loginAsGuest()
                        showGuestDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                ) {
                    Text("Lanjutkan Mode Tamu", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit,
    onSelectAccount: (name: String, email: String) -> Unit
) {
    val accounts = listOf(
        Pair("Lulut Solehhudin", "lulut.solehhudin19@guru.sd.belajar.id"),
        Pair("Lulut Personal", "lulut.solehhudin@gmail.com")
    )
    var customName by remember { mutableStateOf("") }
    var customEmail by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEA4335),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Pilih Akun Google")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih akun Google Anda untuk masuk ke MyEvo dan mengaktifkan sinkronisasi awan:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!isAddingNew) {
                    accounts.forEach { (name, email) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAccount(name, email) }
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(NeonIndigo),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    TextButton(onClick = { isAddingNew = true }) {
                        Text("+ Gunakan akun Google lain")
                    }
                } else {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nama Lengkap") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Alamat Email Google (@gmail.com)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (isAddingNew) {
                Button(
                    onClick = {
                        if (customName.isNotBlank() && customEmail.isNotBlank()) {
                            onSelectAccount(customName, customEmail)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("Masuk")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun FacebookSignInDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String) -> Unit
) {
    var fbName by remember { mutableStateOf("Lulut Solehhudin") }
    var fbEmail by remember { mutableStateOf("lulut.fb@facebook.com") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1877F2),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("f", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Masuk dengan Facebook")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Konfirmasi detail profil akun Facebook Anda untuk melanjutkan integrasi:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = fbName,
                    onValueChange = { fbName = it },
                    label = { Text("Nama Akun Facebook") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fbEmail,
                    onValueChange = { fbEmail = it },
                    label = { Text("Email Facebook") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(fbName, fbEmail) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2))
            ) {
                Text("Lanjutkan Masuk", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun PhoneAuthDialog(
    onDismiss: () -> Unit,
    onVerified: (phoneNumber: String) -> Unit
) {
    var phoneInput by remember { mutableStateOf("0812-3456-7890") }
    var otpInput by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (!isOtpSent) "Masuk dengan No. HP" else "Verifikasi Kode OTP")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isOtpSent) {
                    Text(
                        text = "Masukkan nomor telepon Anda. Kami akan mengirimkan 6 digit kode OTP verifikasi.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Nomor HP (contoh: 08123456789)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Kode OTP telah dikirimkan ke $phoneInput (Kode demo: 123456):",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = {
                            otpInput = it
                            otpError = false
                        },
                        label = { Text("6 Digit Kode OTP") },
                        singleLine = true,
                        isError = otpError,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (otpError) {
                        Text(
                            text = "Kode OTP salah! Coba kode 123456.",
                            fontSize = 11.sp,
                            color = Color.Red
                        )
                    }

                    TextButton(onClick = { otpInput = "123456" }) {
                        Text("⚡ Isi Otomatis Kode Demo (123456)")
                    }
                }
            }
        },
        confirmButton = {
            if (!isOtpSent) {
                Button(
                    onClick = {
                        if (phoneInput.isNotBlank()) {
                            isOtpSent = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Kirim Kode OTP")
                }
            } else {
                Button(
                    onClick = {
                        if (otpInput == "123456" || otpInput.length == 6) {
                            onVerified(phoneInput)
                        } else {
                            otpError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                ) {
                    Text("Verifikasi & Masuk")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

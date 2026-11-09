package com.example.ui.screens

import android.content.Context
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.viewmodel.MyEvoViewModel
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * LoginScreen scaffolds authentication options for MyEvo:
 * - Google Sign-In (Credential Manager / Google Identity)
 * - Facebook Login
 * - Phone Number + SMS OTP Login
 * - Guest Login (Offline mode, data is kept strictly on-device without cloud sync)
 */
@Composable
fun LoginScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showGoogleDialog by remember { mutableStateOf(false) }
    var showFacebookDialog by remember { mutableStateOf(false) }
    var showPhoneDialog by remember { mutableStateOf(false) }
    var showGuestDialog by remember { mutableStateOf(false) }
    var isSigningInWithCredentialManager by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerGoogleCredentialManagerLogin() {
        coroutineScope.launch {
            isSigningInWithCredentialManager = true
            authErrorMessage = null
            try {
                val credentialManager = CredentialManager.create(context)
                // Use a default server client ID if configured or fallback to account picker
                val serverClientId = context.getString(R.string.default_web_client_id)
                val googleIdOption = GetSignInWithGoogleOption.Builder(serverClientId)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential

                if (credential is androidx.credentials.CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id
                    val displayName = googleIdTokenCredential.displayName ?: email.substringBefore("@")

                    viewModel.loginWithGoogle(
                        name = displayName,
                        email = email
                    )
                    onLoginSuccess()
                } else {
                    showGoogleDialog = true
                }
            } catch (e: Exception) {
                // If Play Services / Web Client ID is not provisioned or canceled, fallback gracefully to interactive account selection
                showGoogleDialog = true
            } finally {
                isSigningInWithCredentialManager = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("login_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // App Logo & Branding
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

        if (authErrorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = authErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
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
                    onClick = {
                        triggerGoogleCredentialManagerLogin()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF1F2937)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                ) {
                    if (isSigningInWithCredentialManager) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = NeonIndigo,
                            strokeWidth = 2.dp
                        )
                    } else {
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
                }

                // 2. Facebook Sign-In Button
                Button(
                    onClick = { showFacebookDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("facebook_login_button"),
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
                        .testTag("phone_login_button"),
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
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Telepon",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Masuk dengan Nomor HP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // 4. Guest Mode Button
                OutlinedButton(
                    onClick = { showGuestDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("guest_login_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = "Guest",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Masuk sebagai Mode Tamu",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Tes fungsi aplikasi tanpa sinkronisasi cloud",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy & encryption assurance notice
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = EmeraldGreen,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Data Anda dienkripsi AES-256 dan dilindungi kebijakan privasi.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Dialogs
    if (showGoogleDialog) {
        GoogleSignInDialog(
            onDismiss = { showGoogleDialog = false },
            onSelectAccount = { name, email ->
                viewModel.loginWithGoogle(name = name, email = email)
                showGoogleDialog = false
                onLoginSuccess()
            }
        )
    }

    if (showFacebookDialog) {
        FacebookSignInDialog(
            onDismiss = { showFacebookDialog = false },
            onConfirm = { name, email ->
                viewModel.loginWithFacebook(name = name, email = email)
                showFacebookDialog = false
                onLoginSuccess()
            }
        )
    }

    if (showPhoneDialog) {
        PhoneAuthDialog(
            onDismiss = { showPhoneDialog = false },
            onVerified = { phoneNumber ->
                viewModel.loginWithPhone(phoneNumber = phoneNumber)
                showPhoneDialog = false
                onLoginSuccess()
            }
        )
    }

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
                        text = "Mode Tamu memungkinkan Anda menguji seluruh fitur aplikasi MyEvo (jadwal otomatis, kalkulator anggaran & utang, teka-teki silang harian, dan catatan terenkripsi).",
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
                            text = "⚠️ Perhatian: Data disimpan secara offline di perangkat ini dan TIDAK disinkronkan ke server cloud. Anda dapat beralih ke akun resmi kapan saja melalui menu Profil.",
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
                        onLoginSuccess()
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

package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.LeaderboardUser
import com.example.data.model.WeeklyChallenge
import com.example.ui.components.AppHeader
import com.example.ui.components.StatusPill
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonIndigo
import com.example.ui.theme.SunsetPink
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MyEvoViewModel

@Composable
fun ProfileLeaderboardScreen(
    viewModel: MyEvoViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val weeklyChallenges by viewModel.weeklyChallenges.collectAsState()
    val leaderboard = viewModel.leaderboardUsers
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) } // 0: Profil & Analitik, 1: Papan Peringkat & Tantangan

    val weeklyProductivity = listOf(
        Pair("Sen", 4),
        Pair("Sel", 6),
        Pair("Rab", 8),
        Pair("Kam", 5),
        Pair("Jum", 9),
        Pair("Sab", 7),
        Pair("Min", 3)
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Header
            item {
                AppHeader(
                    title = "Profil & Komunitas",
                    subtitle = if (userProfile.isGuest) "Mode Tamu • Papan Peringkat" else "Papan peringkat global & pencapaian",
                    isOnline = isOnline,
                    isSyncing = isSyncing,
                    isDarkMode = userProfile.isDarkMode,
                    isReadingMode = userProfile.isReadingMode,
                    isGuest = userProfile.isGuest,
                    onToggleOnline = { viewModel.toggleOnlineMode() },
                    onSyncClick = { viewModel.triggerCloudBackup() },
                    onToggleDarkMode = { viewModel.toggleDarkMode() },
                    onToggleReadingMode = { viewModel.toggleReadingMode() }
                )
            }

            // Tab switch
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = NeonIndigo,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Profil & Analitik", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Papan Peringkat Global", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Profile Hero Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("profile_hero_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(NeonIndigo, ElectricCyan, CoralOrange)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(userProfile.avatarEmoji, fontSize = 34.sp)
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (userProfile.name.isNotBlank()) userProfile.name else "Pengguna MyEvo",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        StatusPill(
                                            text = when (userProfile.authProvider) {
                                                com.example.data.model.AuthProvider.GOOGLE -> "Akun Google"
                                                com.example.data.model.AuthProvider.FACEBOOK -> "Akun Facebook"
                                                com.example.data.model.AuthProvider.PHONE -> "No. Telepon"
                                                com.example.data.model.AuthProvider.GUEST -> "Mode Tamu (Offline)"
                                                else -> "Tamu"
                                            },
                                            color = if (userProfile.isGuest) AmberGold else EmeraldGreen
                                        )
                                        Text(
                                            text = userProfile.title,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonIndigo
                                        )
                                    }
                                    Text(
                                        text = userProfile.emailOrPhone,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // XP Progress Bar
                            val xpPct = (userProfile.currentXp.toFloat() / userProfile.nextLevelXp).coerceIn(0f, 1f)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Level ${userProfile.level}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = NeonIndigo
                                )
                                Text(
                                    text = "${userProfile.currentXp} / ${userProfile.nextLevelXp} XP",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { xpPct },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = NeonIndigo,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🔥 ${userProfile.streakDays}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Hari Streak", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("✅ ${userProfile.completedTasksCount}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("Tugas Tuntas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("🏅 #3", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AmberGold)
                                        Text("Peringkat Global", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }

                // Social Share Button
                item {
                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "🔥 Pencapaian MyEvo: Saya sudah mempertahankan streak ${userProfile.streakDays} hari dan menyelesaikan ${userProfile.completedTasksCount} tugas di MyEvo! Peringkat Global #3. #MyEvoApp #Productivity"
                                )
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Bagikan Pencapaian MyEvo ke Media Sosial")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("share_achievement_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bagikan Pencapaian ke Media Sosial", fontWeight = FontWeight.Bold)
                    }
                }

                // Badges Gallery
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(
                            text = "Lencana & Badge Unik (${userProfile.activeBadges.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(userProfile.activeBadges) { badgeName ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (badgeName) {
                                                "Master of Focus" -> "🎯"
                                                "Budget Boss" -> "💰"
                                                "Polyglot Pro" -> "🗣️"
                                                "Streak 14 Hari" -> "🔥"
                                                else -> "🏆"
                                            },
                                            fontSize = 18.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = badgeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Weekly Productivity Analytics Chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("weekly_analytics_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📊 Analitik Produktivitas Mingguan",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                StatusPill(text = "Skor: 92/100", color = EmeraldGreen)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyProductivity.forEach { (day, count) ->
                                    val barHeight = (count * 10).dp
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonIndigo
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height(barHeight)
                                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(NeonIndigo, ElectricCyan)
                                                    )
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = day,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Admin Panel Access Shortcut
                item {
                    OutlinedButton(
                        onClick = { viewModel.navigateTo(AppScreen.ADMIN) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("nav_admin_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = NeonIndigo)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Panel Admin Khusus", color = NeonIndigo)
                    }
                }

                // Account Management Actions
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (userProfile.isGuest) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.AUTH) },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Hubungkan Akun Resmi (Google / Facebook / HP)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.logout() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Keluar / Ganti Akun")
                        }

                        TextButton(
                            onClick = { viewModel.clearAllUserData() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Bersihkan Seluruh Data Lokal", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                // TAB 1: PAPAN PERINGKAT GLOBAL & TANTANGAN MINGGUAN
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "🏆 Papan Peringkat Global Mingguan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Kompetisi sehat antar seluruh pengguna aktif MyEvo di seluruh dunia.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                items(leaderboard, key = { it.rank }) { user ->
                    LeaderboardUserRow(user = user)
                }

                // Weekly Challenges Section
                item {
                    Text(
                        text = "🎯 Tantangan Mingguan Berhadiah Eksklusif:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                items(weeklyChallenges, key = { it.id }) { challenge ->
                    WeeklyChallengeCard(
                        challenge = challenge,
                        onClaim = { viewModel.claimChallenge(challenge.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun LeaderboardUserRow(user: LeaderboardUser) {
    val isTop3 = user.rank <= 3
    val rankColor = when (user.rank) {
        1 -> AmberGold
        2 -> Color(0xFF94A3B8)
        3 -> Color(0xFFB45309)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isCurrentUser) NeonIndigo.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (user.isCurrentUser) androidx.compose.foundation.BorderStroke(1.5.dp, NeonIndigo) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (isTop3) rankColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#${user.rank}",
                    color = if (isTop3) Color.White else rankColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = user.avatarEmoji, fontSize = 22.sp)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (user.isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        StatusPill(text = "Anda", color = NeonIndigo)
                    }
                }
                Text(
                    text = "${user.badge} • Lvl ${user.level}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${user.points} Poin",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = AmberGold
            )
        }
    }
}

@Composable
fun WeeklyChallengeCard(
    challenge: WeeklyChallenge,
    onClaim: () -> Unit
) {
    val isComplete = challenge.progress >= challenge.target
    val pct = (challenge.progress.toFloat() / challenge.target).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusPill(text = "+${challenge.rewardPoints} XP", color = AmberGold)
            }

            Text(
                text = challenge.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progres: ${challenge.progress}/${challenge.target}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Berakhir dalam ${challenge.endsInDays} hari",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = { pct },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isComplete) EmeraldGreen else CoralOrange,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (isComplete && !challenge.isClaimed) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onClaim,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Klaim Hadiah Eksklusif (${challenge.rewardPoints} XP)")
                }
            } else if (challenge.isClaimed) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hadiah telah diklaim!", color = EmeraldGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

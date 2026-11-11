package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.AppLockPreferences
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PinLockMode
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FinanceScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LanguageScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.AIPhotoStudioScreen
import com.example.ui.screens.ProfileLeaderboardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MyEvoTheme
import com.example.ui.theme.NeonIndigo
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MyEvoViewModel
import com.example.worker.WorkManagerHelper

data class BottomNavItem(
    val screen: AppScreen,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    private val viewModel: MyEvoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WorkManagerHelper.schedulePeriodicDeadlineCheck(this)
        setContent {
            val userProfile by viewModel.userProfile.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()

            val context = LocalContext.current
            val appLockPrefs = remember { AppLockPreferences(context) }
            val isAppLockEnabled by appLockPrefs.isAppLockEnabled.collectAsState(initial = false)
            val hasConfiguredPin by appLockPrefs.hasConfiguredPin.collectAsState(initial = false)

            var isAppLocked by remember { mutableStateOf(false) }
            var pausedTimestamp by remember { mutableLongStateOf(0L) }

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner, isAppLockEnabled, hasConfiguredPin) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_PAUSE) {
                        pausedTimestamp = System.currentTimeMillis()
                    } else if (event == Lifecycle.Event.ON_RESUME) {
                        if (isAppLockEnabled && hasConfiguredPin && (System.currentTimeMillis() - pausedTimestamp > 1500L) && pausedTimestamp != 0L) {
                            isAppLocked = true
                        }
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            MyEvoTheme(
                darkTheme = userProfile.isDarkMode,
                readingMode = userProfile.isReadingMode
            ) {
                if (isAppLocked) {
                    PinLockScreen(
                        mode = PinLockMode.AUTHENTICATE,
                        onUnlockSuccess = { isAppLocked = false }
                    )
                } else {
                    // BackHandler to return to HOME when in other tabs
                    BackHandler(enabled = currentScreen != AppScreen.HOME && currentScreen != AppScreen.AUTH) {
                        viewModel.navigateTo(AppScreen.HOME)
                    }

                    val navItems = listOf(
                        BottomNavItem(AppScreen.HOME, "Tugas", Icons.Default.CheckCircle, "nav_home"),
                        BottomNavItem(AppScreen.CALENDAR, "Kalender", Icons.Default.CalendarMonth, "nav_calendar"),
                        BottomNavItem(AppScreen.FINANCE, "Keuangan", Icons.Default.Payments, "nav_finance"),
                        BottomNavItem(AppScreen.LEARN, "Belajar", Icons.Default.School, "nav_learn"),
                        BottomNavItem(AppScreen.PROFILE, "Profil", Icons.Default.AccountCircle, "nav_profile")
                    )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentScreen != AppScreen.AUTH) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                            ) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(64.dp)
                                        .testTag("bottom_nav_bar")
                                ) {
                                    navItems.forEach { item ->
                                        val isSelected = currentScreen == item.screen
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { viewModel.navigateTo(item.screen) },
                                            icon = {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = item.title,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = item.title,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = NeonIndigo,
                                                selectedTextColor = NeonIndigo,
                                                indicatorColor = NeonIndigo.copy(alpha = 0.15f),
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag(item.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.AUTH -> LoginScreen(viewModel = viewModel)
                                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                                AppScreen.CALENDAR -> CalendarScreen(viewModel = viewModel)
                                AppScreen.FINANCE -> FinanceScreen(viewModel = viewModel)
                                AppScreen.LEARN -> LanguageScreen(viewModel = viewModel)
                                AppScreen.STUDIO -> AIPhotoStudioScreen(viewModel = viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
                                AppScreen.NOTES -> NotesScreen(viewModel = viewModel)
                                AppScreen.PROFILE -> ProfileLeaderboardScreen(viewModel = viewModel)
                                AppScreen.ADMIN -> AdminScreen(viewModel = viewModel)
                                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel, onBack = { viewModel.navigateTo(AppScreen.HOME) })
                            }
                        }
                    }
                }
                }
            }
        }
    }
}

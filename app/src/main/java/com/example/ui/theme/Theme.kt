package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonIndigo,
    onPrimary = Color.White,
    primaryContainer = DeepViolet,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = ElectricCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF164E63),
    onSecondaryContainer = NeonCyanLight,
    tertiary = CoralOrange,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = DeepViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = DeepViolet,
    secondary = ElectricCyan,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFECFEFF),
    onSecondaryContainer = Color(0xFF0E7490),
    tertiary = CoralOrange,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCard,
    onSurfaceVariant = LightTextSecondary
)

private val ReadingColorScheme = darkColorScheme(
    primary = ReadingPrimary,
    onPrimary = Color.Black,
    primaryContainer = ReadingCard,
    onPrimaryContainer = ReadingTextPrimary,
    secondary = RadiantYellow,
    onSecondary = Color.Black,
    background = ReadingBg,
    onBackground = ReadingTextPrimary,
    surface = ReadingSurface,
    onSurface = ReadingTextPrimary,
    surfaceVariant = ReadingCard,
    onSurfaceVariant = ReadingTextSecondary
)

@Composable
fun MyEvoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    readingMode: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        readingMode -> ReadingColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

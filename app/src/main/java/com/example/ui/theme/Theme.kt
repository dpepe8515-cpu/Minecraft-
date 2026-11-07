package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RoxyCyberColorScheme = darkColorScheme(
    primary = RoxyCyan,
    onPrimary = Color(0xFF00262B),
    primaryContainer = Color(0xFF003B42),
    onPrimaryContainer = RoxyCyan,
    secondary = RoxyPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1D59),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = RoxyEmerald,
    onTertiary = Color(0xFF002B1D),
    background = RoxyObsidian,
    onBackground = RoxyTextPrimary,
    surface = RoxyCarbon,
    onSurface = RoxyTextPrimary,
    surfaceVariant = RoxyCardSurface,
    onSurfaceVariant = RoxyTextSecondary,
    outline = RoxyBorderSubtle,
    error = RoxyRose,
    onError = Color.White
)

private val RoxyMatrixColorScheme = RoxyCyberColorScheme.copy(
    primary = RoxyEmerald,
    onPrimary = Color(0xFF00291B),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFF6EE7B7),
    secondary = RoxyCyan
)

private val RoxyVioletColorScheme = RoxyCyberColorScheme.copy(
    primary = RoxyPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF3B1D7A),
    onPrimaryContainer = Color(0xFFDDD6FE),
    secondary = RoxyCyan
)

@Composable
fun MyApplicationTheme(
    accentIndex: Int = 0,
    content: @Composable () -> Unit
) {
    val colorScheme = when (accentIndex) {
        1 -> RoxyMatrixColorScheme
        2 -> RoxyVioletColorScheme
        else -> RoxyCyberColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

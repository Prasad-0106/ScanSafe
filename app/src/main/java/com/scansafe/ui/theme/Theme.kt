package com.scansafe.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenDark,
    onPrimary = Color(0xFF0F1311), // dark text on light primary in dark mode
    primaryContainer = PrimaryGreenDark.copy(alpha = 0.15f),
    onPrimaryContainer = PrimaryGreenDark,
    secondary = AiTeal,
    onSecondary = Color(0xFF0F1311),
    secondaryContainer = AiTeal.copy(alpha = 0.15f),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = OutlineDark,
    error = HealthScoreScale.E,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreenLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryGreenLight.copy(alpha = 0.1f),
    onPrimaryContainer = PrimaryGreenLight,
    secondary = AiTeal,
    onSecondary = Color.White,
    secondaryContainer = AiTeal.copy(alpha = 0.1f),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,
    outline = OutlineLight,
    error = HealthScoreScale.E,
    onError = Color.White
)

@Composable
fun ScanSafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            var currentContext = view.context
            var activity: Activity? = null
            while (currentContext is android.content.ContextWrapper) {
                if (currentContext is Activity) {
                    activity = currentContext
                    break
                }
                currentContext = currentContext.baseContext
            }
            if (currentContext is Activity) {
                activity = currentContext
            }

            activity?.let { act ->
                val window = act.window
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ScanSafeTypography,
        content = content
    )
}

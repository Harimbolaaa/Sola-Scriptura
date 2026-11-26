package com.harimbola.solascriptura.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalStudyColors = staticCompositionLocalOf {
    AppColorSchemes.getSemanticColors(isDark = false, visualTheme = AppVisualTheme.CLASSIC_PAPER)
}

val LocalAppDimensions = staticCompositionLocalOf {
    AppDimensions()
}

val LocalAnimationsEnabled = staticCompositionLocalOf {
    true
}

@Composable
fun SolaScripturaTheme(
    preferences: ThemePreferences = ThemePreferences(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (preferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val context = LocalContext.current
    val colorScheme = if (
        preferences.useDynamicColors &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        preferences.visualTheme != AppVisualTheme.HIGH_CONTRAST
    ) {
        if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        AppColorSchemes.getColorScheme(
            visualTheme = preferences.visualTheme,
            isDark = useDark,
            accentPreset = preferences.accentPreset
        )
    }

    val semanticColors = AppColorSchemes.getSemanticColors(
        isDark = useDark,
        visualTheme = preferences.visualTheme
    )
    val dimensions = AppDimensions.forDensity(preferences.cardDensity)
    val typography = AppTypography.createTypography(
        preset = preferences.fontStyle,
        fontScale = preferences.fontScale,
        lineSpacingMultiplier = preferences.lineSpacingMultiplier
    )

    CompositionLocalProvider(
        LocalStudyColors provides semanticColors,
        LocalAppDimensions provides dimensions,
        LocalAnimationsEnabled provides preferences.animationsEnabled
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = AppShapes,
            content = content
        )
    }
}

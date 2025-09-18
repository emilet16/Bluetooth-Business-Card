package com.quartier.quartier.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    secondary = secondaryLight,
    onSecondary = textLight,
    error = errorLight,
    onError = textLight,
    surface = surfaceLight,
    onSurface = textLight,
    outline = secondaryLight
)

private val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    secondary = secondaryDark,
    onSecondary = textDark,
    error = errorDark,
    onError = textDark,
    surface = surfaceDark,
    onSurface = textDark,
    outline = secondaryDark
)

@Immutable
data class ExtendedColors(
    val shade: Color,
    val navigation: Color,
    val accent: Color,
    val skeleton: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified
    )
}

val lightExtendedColors = ExtendedColors(
    shadeColor,
    navigationLight,
    accentLight,
    skeletonColor
)

val darkExtendedColors = ExtendedColors(
    shadeColor,
    navigationDark,
    accentDark,
    skeletonColor
)

@Composable
fun QuartierTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    val extendedColors = when {
        darkTheme -> darkExtendedColors
        else -> lightExtendedColors
    }

    CompositionLocalProvider(LocalExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

object ExtendedTheme {
    val colors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
}


package com.kanbe1365.countergraph.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 現在の配色に応じたブランドカラーを配下へ渡す。iOS の Color.brand に相当。 */
val LocalBrandColor = staticCompositionLocalOf { BrandLight }

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    background = Color.Black,
    surface = Color(0xFF1C1C1E),
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColors = lightColorScheme(
    primary = BrandLight,
    onPrimary = Color.White,
    background = Color.White,
    surface = Color(0xFFF2F2F7),
    onBackground = Color.Black,
    onSurface = Color.Black,
)

@Composable
fun CounterGraphTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val brand = if (darkTheme) BrandDark else BrandLight

    CompositionLocalProvider(LocalBrandColor provides brand) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

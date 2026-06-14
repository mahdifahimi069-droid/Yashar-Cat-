package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = RoyalPurple,
    secondary = BrightGold,
    tertiary = MetallicGold,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    primaryContainer = Color(0xFF4C1D95),
    onPrimaryContainer = Color(0xFFEDE9FE),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = RoyalPurple,
    secondary = BrightGold,
    tertiary = MetallicGold,
    background = Color(0xFFFFFFFF), // Pure white background
    surface = Color(0xFFFAFAFA), // Crisp light-gray surface
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color(0xFF1F2937),
    onSurface = Color(0xFF1F2937),
    primaryContainer = LightPurpleBg,
    onPrimaryContainer = Color(0xFF5B21B6),
    secondaryContainer = LightGoldBg,
    onSecondaryContainer = Color(0xFF92400E)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Force false to ensure light theme is default
  dynamicColor: Boolean = false, // Disable dynamic colors to stick to custom purple & gold
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

package com.bkm.mobil.sdk.lite.demo

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object SimulationTheme {

    fun colorScheme(isDarkTheme: Boolean): ColorScheme {
        return if (isDarkTheme) {
            darkColorScheme(
                background = Color(0xFF121212),
                surface = Color(0xFF121212),
                onBackground = Color.White,
                onSurface = Color.White,
                onSurfaceVariant = Color(0xFFB3B3B3),
                primary = Color(0xFFFFFFFF),
                onPrimary = Color.Black
            )
        } else {
            lightColorScheme(
                background = Color.White,
                surface = Color.White,
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F),
                onSurfaceVariant = Color(0xFF49454F),
                primary = Color(0xFF000000),
                onPrimary = Color.White
            )
        }
    }
}

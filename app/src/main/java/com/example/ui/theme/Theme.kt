package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val EmpowerGuardColorScheme = darkColorScheme(
    primary = DeepPurple,
    secondary = AccentPurpleGlow,
    tertiary = StatusOkGreen,
    background = DarkBackground,
    surface = DarkSurface,
    error = EmergencyRed,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = PureWhite
)

@Composable
fun EmpowerGuardTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EmpowerGuardColorScheme,
        typography = Typography,
        content = content
    )
}

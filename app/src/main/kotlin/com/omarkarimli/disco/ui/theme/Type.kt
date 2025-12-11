package com.omarkarimli.disco.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight

val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily, fontWeight = FontWeight.Bold),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily),
)
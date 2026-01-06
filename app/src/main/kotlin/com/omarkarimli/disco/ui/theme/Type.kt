package com.omarkarimli.disco.ui.theme

import androidx.compose.material3.Typography

val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = boldFontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = boldFontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = boldFontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = boldFontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = boldFontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = boldFontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = boldFontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = boldFontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = boldFontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = regularFontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = regularFontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = regularFontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = regularFontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = regularFontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = regularFontFamily),
)
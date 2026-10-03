package com.example.designsandbox.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.designsandbox.designsystem.common.R

private val googleSansFlex = FontFamily(Font(R.font.google_sans_flex))
private val baseline = Typography()
val AppTypography =
  Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = googleSansFlex),
    displayLargeEmphasized = baseline.displayLargeEmphasized.copy(fontFamily = googleSansFlex),
    displayMedium = baseline.displayMedium.copy(fontFamily = googleSansFlex),
    displayMediumEmphasized = baseline.displayMediumEmphasized.copy(fontFamily = googleSansFlex),
    displaySmall = baseline.displaySmall.copy(fontFamily = googleSansFlex),
    displaySmallEmphasized = baseline.displaySmallEmphasized.copy(fontFamily = googleSansFlex),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = googleSansFlex),
    headlineLargeEmphasized = baseline.headlineLargeEmphasized.copy(fontFamily = googleSansFlex),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = googleSansFlex),
    headlineMediumEmphasized = baseline.headlineMediumEmphasized.copy(fontFamily = googleSansFlex),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = googleSansFlex),
    headlineSmallEmphasized = baseline.headlineSmallEmphasized.copy(fontFamily = googleSansFlex),
    titleLarge = baseline.titleLarge.copy(fontFamily = googleSansFlex),
    titleLargeEmphasized = baseline.titleLargeEmphasized.copy(fontFamily = googleSansFlex),
    titleMedium = baseline.titleMedium.copy(fontFamily = googleSansFlex),
    titleMediumEmphasized = baseline.titleMediumEmphasized.copy(fontFamily = googleSansFlex),
    titleSmall = baseline.titleSmall.copy(fontFamily = googleSansFlex),
    titleSmallEmphasized = baseline.titleSmallEmphasized.copy(fontFamily = googleSansFlex),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = googleSansFlex),
    bodyLargeEmphasized = baseline.bodyLargeEmphasized.copy(fontFamily = googleSansFlex),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = googleSansFlex),
    bodyMediumEmphasized = baseline.bodyMediumEmphasized.copy(fontFamily = googleSansFlex),
    bodySmall = baseline.bodySmall.copy(fontFamily = googleSansFlex),
    bodySmallEmphasized = baseline.bodySmallEmphasized.copy(fontFamily = googleSansFlex),
    labelLarge = baseline.labelLarge.copy(fontFamily = googleSansFlex),
    labelLargeEmphasized = baseline.labelLargeEmphasized.copy(fontFamily = googleSansFlex),
    labelMedium = baseline.labelMedium.copy(fontFamily = googleSansFlex),
    labelMediumEmphasized = baseline.labelMediumEmphasized.copy(fontFamily = googleSansFlex),
    labelSmall = baseline.labelSmall.copy(fontFamily = googleSansFlex),
    labelSmallEmphasized = baseline.labelSmallEmphasized.copy(fontFamily = googleSansFlex),
  )

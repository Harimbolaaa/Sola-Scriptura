package com.harimbola.solascriptura.core.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

object AppTypography {

    val CrimsonProFamily = FontFamily(
        Font(R.font.crimson_pro, FontWeight.Normal),
        Font(R.font.crimson_pro, FontWeight.SemiBold),
        Font(R.font.crimson_pro, FontWeight.Bold)
    )

    val SourceSerifFamily = FontFamily(
        Font(R.font.source_serif_4, FontWeight.Normal),
        Font(R.font.source_serif_4, FontWeight.SemiBold),
        Font(R.font.source_serif_4, FontWeight.Bold)
    )

    val PlusJakartaFamily = FontFamily(
        Font(R.font.plus_jakarta_sans, FontWeight.Normal),
        Font(R.font.plus_jakarta_sans, FontWeight.Medium),
        Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
        Font(R.font.plus_jakarta_sans, FontWeight.Bold)
    )

    val JetBrainsMonoFamily = FontFamily(
        Font(R.font.jetbrains_mono, FontWeight.Normal),
        Font(R.font.jetbrains_mono, FontWeight.Medium)
    )

    fun createTypography(
        preset: FontStylePreset,
        fontScale: Float = 1.0f,
        lineSpacingMultiplier: Float = 1.15f
    ): Typography {
        val displayFont = when (preset) {
            FontStylePreset.CLASSIC_SERIF -> CrimsonProFamily
            FontStylePreset.EDITORIAL_SERIF -> SourceSerifFamily
            FontStylePreset.MODERN_SANS -> PlusJakartaFamily
            FontStylePreset.STUDY_MONO -> CrimsonProFamily
        }
        val bodyFont = when (preset) {
            FontStylePreset.CLASSIC_SERIF -> SourceSerifFamily
            FontStylePreset.EDITORIAL_SERIF -> SourceSerifFamily
            FontStylePreset.MODERN_SANS -> PlusJakartaFamily
            FontStylePreset.STUDY_MONO -> JetBrainsMonoFamily
        }
        val labelFont = PlusJakartaFamily

        val scale = fontScale.coerceIn(0.85f, 1.45f)
        val lineMult = lineSpacingMultiplier.coerceIn(1.0f, 1.5f)

        return Typography(
            displaySmall = TextStyle(
                fontFamily = displayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (32f * scale).sp,
                lineHeight = (38f * scale * lineMult / 1.15f).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = displayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (26f * scale).sp,
                lineHeight = (32f * scale * lineMult / 1.15f).sp
            ),
            headlineSmall = TextStyle(
                fontFamily = displayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (22f * scale).sp,
                lineHeight = (28f * scale * lineMult / 1.15f).sp
            ),
            titleLarge = TextStyle(
                fontFamily = displayFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (20f * scale).sp,
                lineHeight = (26f * scale * lineMult / 1.15f).sp
            ),
            titleMedium = TextStyle(
                fontFamily = labelFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (16f * scale).sp,
                lineHeight = (22f * scale * lineMult / 1.15f).sp
            ),
            titleSmall = TextStyle(
                fontFamily = labelFont,
                fontWeight = FontWeight.Medium,
                fontSize = (14f * scale).sp,
                lineHeight = (20f * scale).sp
            ),
            bodyLarge = TextStyle(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Normal,
                fontSize = (17f * scale).sp,
                lineHeight = (25f * scale * lineMult / 1.15f).sp
            ),
            bodyMedium = TextStyle(
                fontFamily = bodyFont,
                fontWeight = FontWeight.Normal,
                fontSize = (15f * scale).sp,
                lineHeight = (22f * scale * lineMult / 1.15f).sp
            ),
            bodySmall = TextStyle(
                fontFamily = labelFont,
                fontWeight = FontWeight.Normal,
                fontSize = (13f * scale).sp,
                lineHeight = (18f * scale * lineMult / 1.15f).sp
            ),
            labelLarge = TextStyle(
                fontFamily = labelFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = (14f * scale).sp,
                lineHeight = (20f * scale).sp
            ),
            labelMedium = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Medium,
                fontSize = (12f * scale).sp,
                lineHeight = (16f * scale).sp
            ),
            labelSmall = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Normal,
                fontSize = (11f * scale).sp,
                lineHeight = (14f * scale).sp
            )
        )
    }
}

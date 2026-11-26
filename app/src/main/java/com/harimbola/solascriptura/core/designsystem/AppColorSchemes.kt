package com.harimbola.solascriptura.core.designsystem

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class SemanticStudyColors(
    val ironyContainer: Color,
    val onIronyContainer: Color,
    val elenchusContainer: Color,
    val onElenchusContainer: Color,
    val maieuticsContainer: Color,
    val onMaieuticsContainer: Color,
    val synthesisContainer: Color,
    val onSynthesisContainer: Color,
    val bibleLayerAccent: Color,
    val contextLayerAccent: Color,
    val interpretationLayerAccent: Color,
    val personalLayerAccent: Color,
    val completedNodeColor: Color,
    val inProgressNodeColor: Color
)

object AppColorSchemes {

    private fun accentTokens(preset: AccentPreset, isDark: Boolean): Pair<Color, Color> = when (preset) {
        AccentPreset.ANTIQUE_GOLD -> if (isDark) Color(0xFFE0BC6D) to Color(0xFF3E2D00) else Color(0xFF8C6616) to Color(0xFFFFFFFF)
        AccentPreset.SAGE_GREEN -> if (isDark) Color(0xFF9ACFA8) to Color(0xFF0A391E) else Color(0xFF386649) to Color(0xFFFFFFFF)
        AccentPreset.TERRACOTTA -> if (isDark) Color(0xFFF2AA8E) to Color(0xFF4F1D0A) else Color(0xFFA84C2C) to Color(0xFFFFFFFF)
        AccentPreset.COBALT_INK -> if (isDark) Color(0xFFA4C8FF) to Color(0xFF082E62) else Color(0xFF28558F) to Color(0xFFFFFFFF)
    }

    fun getColorScheme(
        visualTheme: AppVisualTheme,
        isDark: Boolean,
        accentPreset: AccentPreset
    ): ColorScheme {
        val (accent, onAccent) = accentTokens(accentPreset, isDark)
        val base = when (visualTheme) {
            AppVisualTheme.CLASSIC_PAPER -> if (isDark) classicPaperDark else classicPaperLight
            AppVisualTheme.MIDNIGHT_STUDY -> if (isDark) midnightStudyDark else midnightStudyLight
            AppVisualTheme.SAGE_NATURE -> if (isDark) sageNatureDark else sageNatureLight
            AppVisualTheme.WARM_SAND -> if (isDark) warmSandDark else warmSandLight
            AppVisualTheme.MINIMAL_CLOUD -> if (isDark) minimalCloudDark else minimalCloudLight
            AppVisualTheme.HIGH_CONTRAST -> if (isDark) highContrastDark else highContrastLight
        }
        return if (visualTheme == AppVisualTheme.HIGH_CONTRAST) {
            base
        } else {
            base.copy(
                primary = accent,
                onPrimary = onAccent
            )
        }
    }

    fun getSemanticColors(isDark: Boolean, visualTheme: AppVisualTheme): SemanticStudyColors {
        if (visualTheme == AppVisualTheme.HIGH_CONTRAST) {
            return if (isDark) {
                SemanticStudyColors(
                    ironyContainer = Color(0xFF1F2A44),
                    onIronyContainer = Color(0xFFFFFFFF),
                    elenchusContainer = Color(0xFF3B2314),
                    onElenchusContainer = Color(0xFFFFFFFF),
                    maieuticsContainer = Color(0xFF133321),
                    onMaieuticsContainer = Color(0xFFFFFFFF),
                    synthesisContainer = Color(0xFF332908),
                    onSynthesisContainer = Color(0xFFFFFFFF),
                    bibleLayerAccent = Color(0xFFFFD54F),
                    contextLayerAccent = Color(0xFF80D8FF),
                    interpretationLayerAccent = Color(0xFFB9F6CA),
                    personalLayerAccent = Color(0xFFFFAB91),
                    completedNodeColor = Color(0xFF69F0AE),
                    inProgressNodeColor = Color(0xFFFFD54F)
                )
            } else {
                SemanticStudyColors(
                    ironyContainer = Color(0xFFE3ECFF),
                    onIronyContainer = Color(0xFF000000),
                    elenchusContainer = Color(0xFFFFE6D6),
                    onElenchusContainer = Color(0xFF000000),
                    maieuticsContainer = Color(0xFFDCF5E4),
                    onMaieuticsContainer = Color(0xFF000000),
                    synthesisContainer = Color(0xFFFFF4C2),
                    onSynthesisContainer = Color(0xFF000000),
                    bibleLayerAccent = Color(0xFF7A5200),
                    contextLayerAccent = Color(0xFF004E8C),
                    interpretationLayerAccent = Color(0xFF1B5E20),
                    personalLayerAccent = Color(0xFF8E2400),
                    completedNodeColor = Color(0xFF1B5E20),
                    inProgressNodeColor = Color(0xFF8C5A00)
                )
            }
        }
        return if (isDark) {
            SemanticStudyColors(
                ironyContainer = Color(0xFF233044),
                onIronyContainer = Color(0xFFD6E3FF),
                elenchusContainer = Color(0xFF3D2A21),
                onElenchusContainer = Color(0xFFFFDBD0),
                maieuticsContainer = Color(0xFF1E3629),
                onMaieuticsContainer = Color(0xFFC5ECD3),
                synthesisContainer = Color(0xFF3A301B),
                onSynthesisContainer = Color(0xFFF6E2B0),
                bibleLayerAccent = Color(0xFFE0BC6D),
                contextLayerAccent = Color(0xFF8CB4E8),
                interpretationLayerAccent = Color(0xFF8DC9A0),
                personalLayerAccent = Color(0xFFE4A188),
                completedNodeColor = Color(0xFF6CC08E),
                inProgressNodeColor = Color(0xFFE0BC6D)
            )
        } else {
            SemanticStudyColors(
                ironyContainer = Color(0xFFE6EEFA),
                onIronyContainer = Color(0xFF1A2C46),
                elenchusContainer = Color(0xFFFAE8DF),
                onElenchusContainer = Color(0xFF4A2517),
                maieuticsContainer = Color(0xFFE2F1E7),
                onMaieuticsContainer = Color(0xFF173824),
                synthesisContainer = Color(0xFFF8EED5),
                onSynthesisContainer = Color(0xFF47350C),
                bibleLayerAccent = Color(0xFF8C6616),
                contextLayerAccent = Color(0xFF28558F),
                interpretationLayerAccent = Color(0xFF2E6B47),
                personalLayerAccent = Color(0xFFA0482A),
                completedNodeColor = Color(0xFF2E7D4F),
                inProgressNodeColor = Color(0xFFB57B14)
            )
        }
    }

    // 1. CLASSIC PAPER
    private val classicPaperLight = lightColorScheme(
        primary = Color(0xFF8C6616),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF5E5BC),
        onPrimaryContainer = Color(0xFF2D1E00),
        secondary = Color(0xFF5D4B3A),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEBDCCB),
        onSecondaryContainer = Color(0xFF24190E),
        tertiary = Color(0xFF3F614D),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFD0E7D7),
        onTertiaryContainer = Color(0xFF0E2618),
        background = Color(0xFFF9F4E8),
        onBackground = Color(0xFF2C2218),
        surface = Color(0xFFFBF7EE),
        onSurface = Color(0xFF2C2218),
        surfaceVariant = Color(0xFFEDE4D2),
        onSurfaceVariant = Color(0xFF5C4E3E),
        surfaceContainerLowest = Color(0xFFFFFDF8),
        surfaceContainerLow = Color(0xFFF6EFE0),
        surfaceContainer = Color(0xFFF0E7D5),
        surfaceContainerHigh = Color(0xFFE8DEC9),
        outline = Color(0xFFB6A891),
        outlineVariant = Color(0xFFDDD2BE),
        error = Color(0xFFA83226),
        onError = Color(0xFFFFFFFF)
    )

    private val classicPaperDark = darkColorScheme(
        primary = Color(0xFFE0BC6D),
        onPrimary = Color(0xFF3E2D00),
        primaryContainer = Color(0xFF584210),
        onPrimaryContainer = Color(0xFFFCE9B8),
        secondary = Color(0xFFCBBBA8),
        onSecondary = Color(0xFF33271B),
        secondaryContainer = Color(0xFF493B2D),
        onSecondaryContainer = Color(0xFFEADBC8),
        tertiary = Color(0xFF9ACFA8),
        onTertiary = Color(0xFF103521),
        background = Color(0xFF181410),
        onBackground = Color(0xFFF1E8D8),
        surface = Color(0xFF1F1A14),
        onSurface = Color(0xFFF1E8D8),
        surfaceVariant = Color(0xFF362E24),
        onSurfaceVariant = Color(0xFFC8BCA8),
        surfaceContainerLowest = Color(0xFF13100C),
        surfaceContainerLow = Color(0xFF241E17),
        surfaceContainer = Color(0xFF2A231B),
        surfaceContainerHigh = Color(0xFF342C22),
        outline = Color(0xFF7B6F5E),
        outlineVariant = Color(0xFF463D31),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005)
    )

    // 2. MIDNIGHT STUDY
    private val midnightStudyLight = lightColorScheme(
        primary = Color(0xFF2B4C7E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E4F8),
        onPrimaryContainer = Color(0xFF0E2344),
        secondary = Color(0xFF8B6B28),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3E2B8),
        onSecondaryContainer = Color(0xFF2F2100),
        tertiary = Color(0xFF39656B),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFF1F4FA),
        onBackground = Color(0xFF141B29),
        surface = Color(0xFFF8FAFF),
        onSurface = Color(0xFF141B29),
        surfaceVariant = Color(0xFFE0E6F2),
        onSurfaceVariant = Color(0xFF465066),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFEDF1F9),
        surfaceContainer = Color(0xFFE5EBF5),
        surfaceContainerHigh = Color(0xFFDCE3F0),
        outline = Color(0xFF98A3BA),
        outlineVariant = Color(0xFFCBD3E3)
    )

    private val midnightStudyDark = darkColorScheme(
        primary = Color(0xFFE0BC6D),
        onPrimary = Color(0xFF2A1E00),
        primaryContainer = Color(0xFF223454),
        onPrimaryContainer = Color(0xFFEAF0FF),
        secondary = Color(0xFFA7C4F2),
        onSecondary = Color(0xFF102849),
        secondaryContainer = Color(0xFF1D2F4C),
        onSecondaryContainer = Color(0xFFD8E4FA),
        tertiary = Color(0xFF8ED0D6),
        onTertiary = Color(0xFF07363B),
        background = Color(0xFF0D1522),
        onBackground = Color(0xFFE8EEF8),
        surface = Color(0xFF131D2E),
        onSurface = Color(0xFFE8EEF8),
        surfaceVariant = Color(0xFF202D44),
        onSurfaceVariant = Color(0xFFB3C0D6),
        surfaceContainerLowest = Color(0xFF090F1A),
        surfaceContainerLow = Color(0xFF172236),
        surfaceContainer = Color(0xFF1C2940),
        surfaceContainerHigh = Color(0xFF24334F),
        outline = Color(0xFF536382),
        outlineVariant = Color(0xFF2D3C59)
    )

    // 3. SAGE NATURE
    private val sageNatureLight = lightColorScheme(
        primary = Color(0xFF386649),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD3EAD8),
        onPrimaryContainer = Color(0xFF0C2B17),
        secondary = Color(0xFF5F6B50),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE2E9D5),
        onSecondaryContainer = Color(0xFF1E2615),
        tertiary = Color(0xFF7D5D3B),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFF3F6F0),
        onBackground = Color(0xFF19241B),
        surface = Color(0xFFF8FAF5),
        onSurface = Color(0xFF19241B),
        surfaceVariant = Color(0xFFE1EAE0),
        onSurfaceVariant = Color(0xFF445246),
        surfaceContainerLowest = Color(0xFFFCFDF9),
        surfaceContainerLow = Color(0xFFEEF3EC),
        surfaceContainer = Color(0xFFE6EDE4),
        surfaceContainerHigh = Color(0xFFDCE6DA),
        outline = Color(0xFF8FA091),
        outlineVariant = Color(0xFFC5D3C5)
    )

    private val sageNatureDark = darkColorScheme(
        primary = Color(0xFF9ACFA8),
        onPrimary = Color(0xFF0A391E),
        primaryContainer = Color(0xFF234D33),
        onPrimaryContainer = Color(0xFFD6EEDC),
        secondary = Color(0xFFB8C7A8),
        onSecondary = Color(0xFF23301A),
        secondaryContainer = Color(0xFF33422A),
        onSecondaryContainer = Color(0xFFDDE8D0),
        tertiary = Color(0xFFE0BC92),
        onTertiary = Color(0xFF3F2910),
        background = Color(0xFF111913),
        onBackground = Color(0xFFE2ECE3),
        surface = Color(0xFF17211A),
        onSurface = Color(0xFFE2ECE3),
        surfaceVariant = Color(0xFF26352A),
        onSurfaceVariant = Color(0xFFB1C2B4),
        surfaceContainerLowest = Color(0xFF0C120E),
        surfaceContainerLow = Color(0xFF1B271F),
        surfaceContainer = Color(0xFF212F26),
        surfaceContainerHigh = Color(0xFF2A3A2F),
        outline = Color(0xFF5D7061),
        outlineVariant = Color(0xFF354539)
    )

    // 4. WARM SAND
    private val warmSandLight = lightColorScheme(
        primary = Color(0xFFA84C2C),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFF9DDD2),
        onPrimaryContainer = Color(0xFF3F1304),
        secondary = Color(0xFF705644),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFEEE0D3),
        onSecondaryContainer = Color(0xFF291B11),
        tertiary = Color(0xFF576B4B),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFF6EFE7),
        onBackground = Color(0xFF2E2119),
        surface = Color(0xFFFBF6F0),
        onSurface = Color(0xFF2E2119),
        surfaceVariant = Color(0xFFEBE0D4),
        onSurfaceVariant = Color(0xFF5E4A3E),
        surfaceContainerLowest = Color(0xFFFFFBF7),
        surfaceContainerLow = Color(0xFFF3EAE0),
        surfaceContainer = Color(0xFFECE1D5),
        surfaceContainerHigh = Color(0xFFE4D6C8),
        outline = Color(0xFFB59E8E),
        outlineVariant = Color(0xFFDACBBE)
    )

    private val warmSandDark = darkColorScheme(
        primary = Color(0xFFF2AA8E),
        onPrimary = Color(0xFF4F1D0A),
        primaryContainer = Color(0xFF6E2E17),
        onPrimaryContainer = Color(0xFFFFDFD3),
        secondary = Color(0xFFD6BEB0),
        onSecondary = Color(0xFF3A281E),
        secondaryContainer = Color(0xFF4F3B2F),
        onSecondaryContainer = Color(0xFFF2DFD3),
        tertiary = Color(0xFFB5CFA6),
        onTertiary = Color(0xFF223618),
        background = Color(0xFF1A130F),
        onBackground = Color(0xFFF3E7DD),
        surface = Color(0xFF221914),
        onSurface = Color(0xFFF3E7DD),
        surfaceVariant = Color(0xFF382A22),
        onSurfaceVariant = Color(0xFFCAB7AA),
        surfaceContainerLowest = Color(0xFF140E0B),
        surfaceContainerLow = Color(0xFF271D18),
        surfaceContainer = Color(0xFF2F231D),
        surfaceContainerHigh = Color(0xFF392B24),
        outline = Color(0xFF816D60),
        outlineVariant = Color(0xFF4A3B32)
    )

    // 5. MINIMAL CLOUD
    private val minimalCloudLight = lightColorScheme(
        primary = Color(0xFF1F2937),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE5E7EB),
        onPrimaryContainer = Color(0xFF111827),
        secondary = Color(0xFF4B5563),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFF3F4F6),
        onSecondaryContainer = Color(0xFF1F2937),
        tertiary = Color(0xFF2563EB),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFAFAFB),
        onBackground = Color(0xFF111827),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111827),
        surfaceVariant = Color(0xFFF1F3F5),
        onSurfaceVariant = Color(0xFF4B5563),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF7F8FA),
        surfaceContainer = Color(0xFFF1F3F6),
        surfaceContainerHigh = Color(0xFFE8EBF0),
        outline = Color(0xFFD1D5DB),
        outlineVariant = Color(0xFFE5E7EB)
    )

    private val minimalCloudDark = darkColorScheme(
        primary = Color(0xFFE5E7EB),
        onPrimary = Color(0xFF111827),
        primaryContainer = Color(0xFF374151),
        onPrimaryContainer = Color(0xFFF9FAFB),
        secondary = Color(0xFFD1D5DB),
        onSecondary = Color(0xFF1F2937),
        secondaryContainer = Color(0xFF262B33),
        onSecondaryContainer = Color(0xFFE5E7EB),
        tertiary = Color(0xFF93C5FD),
        onTertiary = Color(0xFF0B254A),
        background = Color(0xFF101216),
        onBackground = Color(0xFFF3F4F6),
        surface = Color(0xFF16191F),
        onSurface = Color(0xFFF3F4F6),
        surfaceVariant = Color(0xFF222730),
        onSurfaceVariant = Color(0xFF9CA3AF),
        surfaceContainerLowest = Color(0xFF0C0E11),
        surfaceContainerLow = Color(0xFF1B1F27),
        surfaceContainer = Color(0xFF212630),
        surfaceContainerHigh = Color(0xFF2B313D),
        outline = Color(0xFF4B5563),
        outlineVariant = Color(0xFF2E3440)
    )

    // 6. HIGH CONTRAST
    private val highContrastLight = lightColorScheme(
        primary = Color(0xFF002E6E),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6E4FF),
        onPrimaryContainer = Color(0xFF000000),
        secondary = Color(0xFF522D00),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFE5B8),
        onSecondaryContainer = Color(0xFF000000),
        tertiary = Color(0xFF00471B),
        onTertiary = Color(0xFFFFFFFF),
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF000000),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF000000),
        surfaceVariant = Color(0xFFEDEDED),
        onSurfaceVariant = Color(0xFF1A1A1A),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF5F5F5),
        surfaceContainer = Color(0xFFEBEBEB),
        surfaceContainerHigh = Color(0xFFE0E0E0),
        outline = Color(0xFF000000),
        outlineVariant = Color(0xFF555555),
        error = Color(0xFF990000),
        onError = Color(0xFFFFFFFF)
    )

    private val highContrastDark = darkColorScheme(
        primary = Color(0xFFFFD54F),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF2B2200),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFF80D8FF),
        onSecondary = Color(0xFF000000),
        secondaryContainer = Color(0xFF002B3D),
        onSecondaryContainer = Color(0xFFFFFFFF),
        tertiary = Color(0xFF69F0AE),
        onTertiary = Color(0xFF000000),
        background = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        surface = Color(0xFF0A0A0A),
        onSurface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFF1C1C1C),
        onSurfaceVariant = Color(0xFFF5F5F5),
        surfaceContainerLowest = Color(0xFF000000),
        surfaceContainerLow = Color(0xFF121212),
        surfaceContainer = Color(0xFF1A1A1A),
        surfaceContainerHigh = Color(0xFF262626),
        outline = Color(0xFFFFFFFF),
        outlineVariant = Color(0xFFAAAAAA),
        error = Color(0xFFFF6E6E),
        onError = Color(0xFF000000)
    )
}

package com.harimbola.solascriptura.core.designsystem

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppDimensions(
    val screenPadding: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val sectionSpacing: Dp = 20.dp,
    val itemSpacing: Dp = 12.dp,
    val minTouchTarget: Dp = 48.dp,
    val maxContentWidth: Dp = 680.dp
) {
    companion object {
        fun forDensity(density: CardDensity): AppDimensions = when (density) {
            CardDensity.COMPACT -> AppDimensions(
                screenPadding = 12.dp,
                cardPadding = 12.dp,
                sectionSpacing = 14.dp,
                itemSpacing = 8.dp
            )
            CardDensity.COMFORTABLE -> AppDimensions(
                screenPadding = 16.dp,
                cardPadding = 16.dp,
                sectionSpacing = 20.dp,
                itemSpacing = 12.dp
            )
            CardDensity.SPACIOUS -> AppDimensions(
                screenPadding = 20.dp,
                cardPadding = 22.dp,
                sectionSpacing = 28.dp,
                itemSpacing = 16.dp
            )
        }
    }
}

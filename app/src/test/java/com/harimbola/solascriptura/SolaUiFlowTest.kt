package com.harimbola.solascriptura

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.harimbola.solascriptura.core.designsystem.AccentPreset
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.DiscourseLayer
import com.harimbola.solascriptura.core.designsystem.DiscourseLayerPill
import com.harimbola.solascriptura.core.designsystem.SolaScripturaTheme
import com.harimbola.solascriptura.core.designsystem.ThemePreferences
import com.harimbola.solascriptura.core.designsystem.ThemePreviewCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "fr", application = android.app.Application::class)
class SolaUiFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `theme preview cards switch selection and discourse layers render distinctly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var selectedTheme = AppVisualTheme.CLASSIC_PAPER

        composeTestRule.setContent {
            SolaScripturaTheme(
                preferences = ThemePreferences(visualTheme = selectedTheme)
            ) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    AppVisualTheme.entries.forEach { theme ->
                        ThemePreviewCard(
                            visualTheme = theme,
                            isSelected = selectedTheme == theme,
                            isDarkPreview = false,
                            locale = "fr",
                            accentPreset = AccentPreset.ANTIQUE_GOLD,
                            sampleQuestion = "Question test",
                            onSelect = { selectedTheme = theme }
                        )
                    }
                    DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
                    DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
                    DiscourseLayerPill(DiscourseLayer.PROPOSED_INTERPRETATION)
                    DiscourseLayerPill(DiscourseLayer.PERSONAL_OPINION)
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_preview_midnight_study")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        assertEquals(AppVisualTheme.MIDNIGHT_STUDY, selectedTheme)

        composeTestRule.onNodeWithText(context.getString(R.string.dialogue_layer_bible))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.dialogue_layer_interpretation))
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.dialogue_layer_personal))
            .performScrollTo()
            .assertIsDisplayed()
    }
}

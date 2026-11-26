package com.harimbola.solascriptura.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.core.designsystem.ThemeMode
import com.harimbola.solascriptura.core.designsystem.ThemePreviewCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val scrollState = rememberScrollState()
    val isDarkPreview = prefs.theme.themeMode == ThemeMode.DARK

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = dims.maxContentWidth)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = dims.screenPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding_dialogue),
                    contentDescription = stringResource(R.string.onboarding_title),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(172.dp)
                        .clip(MaterialTheme.shapes.large)
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.onboarding_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.onboarding_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Language selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.onboarding_language_label),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("fr" to "Français", "mg" to "Malagasy", "en" to "English").forEach { (code, label) ->
                            FilterChip(
                                selected = prefs.study.language == code,
                                onClick = { viewModel.selectLanguage(code) },
                                label = { Text(label) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("onboarding_lang_$code")
                            )
                        }
                    }
                }

                // Light / Dark / System mode toggle
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    val modes = listOf(
                        ThemeMode.LIGHT to Pair("Clair", Icons.Rounded.LightMode),
                        ThemeMode.DARK to Pair("Sombre", Icons.Rounded.DarkMode),
                        ThemeMode.SYSTEM to Pair("Système", null)
                    )
                    modes.forEachIndexed { idx, (mode, labelIcon) ->
                        SegmentedButton(
                            selected = prefs.theme.themeMode == mode,
                            onClick = { viewModel.selectThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = idx, count = modes.size),
                            modifier = Modifier.testTag("onboarding_mode_${mode.name.lowercase()}"),
                            icon = {
                                labelIcon.second?.let {
                                    Icon(it, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        ) {
                            Text(labelIcon.first)
                        }
                    }
                }

                // 6 Visual Themes live preview grid
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    maxItemsInEachRow = 2
                ) {
                    val sampleQ = stringResource(R.string.onboarding_preview_sample)
                    AppVisualTheme.entries.forEach { visualTheme ->
                        ThemePreviewCard(
                            visualTheme = visualTheme,
                            isSelected = prefs.theme.visualTheme == visualTheme,
                            isDarkPreview = isDarkPreview,
                            locale = prefs.study.language,
                            accentPreset = prefs.theme.accentPreset,
                            sampleQuestion = sampleQ,
                            onSelect = { viewModel.selectTheme(visualTheme) },
                            modifier = Modifier.weight(1f, fill = true)
                        )
                    }
                }

                // Methodological explanation + SDA perspective notice
                SolaSurfaceCard(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Text(
                            text = stringResource(R.string.onboarding_method_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_method_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.onboarding_perspective_note),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Button(
                    onClick = {
                        viewModel.completeOnboarding()
                        onFinished()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_start_button")
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_start_button),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

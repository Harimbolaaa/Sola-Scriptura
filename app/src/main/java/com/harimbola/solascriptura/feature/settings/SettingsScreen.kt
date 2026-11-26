package com.harimbola.solascriptura.feature.settings

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.AccentPreset
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.CardDensity
import com.harimbola.solascriptura.core.designsystem.FontStylePreset
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.core.designsystem.SyncMode
import com.harimbola.solascriptura.core.designsystem.ThemeMode
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onOpenAccount: () -> Unit,
    onOpenNotebookExport: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val prefs = state.preferences
    val dims = LocalAppDimensions.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showConfirmDeleteAll by remember { mutableStateOf(false) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = dims.maxContentWidth)
                    .fillMaxSize(),
                contentPadding = PaddingValues(dims.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                // 1. Appearance
                item {
                    SolaSurfaceCard {
                        Text(
                            text = stringResource(R.string.settings_section_appearance),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Thème visuel", style = MaterialTheme.typography.labelLarge)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            AppVisualTheme.entries.forEach { theme ->
                                FilterChip(
                                    selected = prefs.theme.visualTheme == theme,
                                    onClick = { viewModel.setVisualTheme(theme) },
                                    label = { Text(theme.label(prefs.study.language)) },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("settings_theme_${theme.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Mode", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                ThemeMode.LIGHT to "Clair",
                                ThemeMode.DARK to "Sombre",
                                ThemeMode.SYSTEM to "Système"
                            ).forEach { (mode, label) ->
                                FilterChip(
                                    selected = prefs.theme.themeMode == mode,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("settings_mode_${mode.name.lowercase()}")
                                )
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            SettingSwitchRow(
                                label = "Couleurs dynamiques Material You",
                                checked = prefs.theme.useDynamicColors,
                                testTag = "settings_dynamic_color_switch",
                                onCheckedChange = viewModel::setDynamicColors
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Accentuation", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentPreset.entries.forEach { preset ->
                                FilterChip(
                                    selected = prefs.theme.accentPreset == preset,
                                    onClick = { viewModel.setAccentPreset(preset) },
                                    label = { Text(preset.labelFr) },
                                    modifier = Modifier.testTag("settings_accent_${preset.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Typographie", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FontStylePreset.entries.forEach { font ->
                                FilterChip(
                                    selected = prefs.theme.fontStyle == font,
                                    onClick = { viewModel.setFontStyle(font) },
                                    label = { Text(font.labelFr) },
                                    modifier = Modifier.testTag("settings_font_${font.name.lowercase()}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Taille du texte (${(prefs.theme.fontScale * 100).roundToInt()}%)",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = prefs.theme.fontScale,
                            onValueChange = viewModel::setFontScale,
                            valueRange = 0.85f..1.35f,
                            modifier = Modifier.testTag("settings_font_scale_slider")
                        )

                        Text(
                            text = "Interligne (${String.format("%.2f", prefs.theme.lineSpacingMultiplier)}x)",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Slider(
                            value = prefs.theme.lineSpacingMultiplier,
                            onValueChange = viewModel::setLineSpacing,
                            valueRange = 1.0f..1.45f,
                            modifier = Modifier.testTag("settings_line_spacing_slider")
                        )

                        Text("Densité des cartes", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CardDensity.entries.forEach { density ->
                                FilterChip(
                                    selected = prefs.theme.cardDensity == density,
                                    onClick = { viewModel.setCardDensity(density) },
                                    label = { Text(density.labelFr) },
                                    modifier = Modifier.testTag("settings_density_${density.name.lowercase()}")
                                )
                            }
                        }

                        SettingSwitchRow(
                            label = "Animations subtiles",
                            checked = prefs.theme.animationsEnabled,
                            testTag = "settings_animations_switch",
                            onCheckedChange = viewModel::setAnimationsEnabled
                        )
                    }
                }

                // 2. Study Preferences
                item {
                    SolaSurfaceCard {
                        Text(
                            text = stringResource(R.string.settings_section_study),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Langue", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("fr" to "Français", "mg" to "Malagasy", "en" to "English").forEach { (code, label) ->
                                FilterChip(
                                    selected = prefs.study.language == code,
                                    onClick = { viewModel.setLanguage(code) },
                                    label = { Text(label) },
                                    modifier = Modifier.testTag("settings_lang_$code")
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Durée préférée", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(10, 15, 25).forEach { mins ->
                                FilterChip(
                                    selected = prefs.study.preferredDurationMinutes == mins,
                                    onClick = { viewModel.setPreferredDuration(mins) },
                                    label = { Text("$mins min") },
                                    modifier = Modifier.testTag("settings_duration_$mins")
                                )
                            }
                        }
                        SettingSwitchRow(
                            label = "Reprendre automatiquement la dernière étude",
                            checked = prefs.study.autoResumeLastStudy,
                            testTag = "settings_auto_resume_switch",
                            onCheckedChange = viewModel::setAutoResume
                        )
                        SettingSwitchRow(
                            label = "Afficher les références bibliques",
                            checked = prefs.study.showVerseReferences,
                            testTag = "settings_show_refs_switch",
                            onCheckedChange = viewModel::setShowVerseReferences
                        )
                        SettingSwitchRow(
                            label = "Afficher le contexte historique",
                            checked = prefs.study.showBiblicalContext,
                            testTag = "settings_show_context_switch",
                            onCheckedChange = viewModel::setShowBiblicalContext
                        )
                    }
                }

                // 3. Account & Sync
                item {
                    SolaSurfaceCard {
                        Text(
                            text = stringResource(R.string.settings_section_account),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenAccount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_open_account_button")
                        ) {
                            Icon(Icons.Rounded.AccountCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                prefs.accountEmail ?: stringResource(R.string.account_title),
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Mode de synchronisation", style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            SyncMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = prefs.sync.syncMode == mode,
                                    onClick = { viewModel.setSyncMode(mode) },
                                    label = { Text(mode.labelFr) },
                                    modifier = Modifier.testTag("settings_sync_${mode.name.lowercase()}")
                                )
                            }
                        }
                        SettingSwitchRow(
                            label = "Synchroniser les préférences visuelles",
                            checked = prefs.sync.syncSettingsEnabled,
                            testTag = "settings_sync_prefs_switch",
                            onCheckedChange = viewModel::setSyncSettingsEnabled
                        )
                        Text(
                            text = "Éléments en file locale : ${state.pendingSyncCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 4. Offline Storage
                item {
                    SolaSurfaceCard {
                        Text(
                            text = stringResource(R.string.settings_section_storage),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val kb = (state.totalStorageBytes / 1024L).coerceAtLeast(1L)
                        Text(
                            text = "${state.installedStudiesCount} étude(s) installée(s) · $kb Ko en base locale",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = { viewModel.clearDownloadedCache() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_clear_cache_button")
                        ) {
                            Icon(Icons.Rounded.CleaningServices, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nettoyer les études téléchargées")
                        }
                    }
                }

                // 5. Privacy & Data
                item {
                    SolaSurfaceCard {
                        Text(
                            text = stringResource(R.string.settings_section_privacy),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onOpenNotebookExport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_export_notes_button")
                        ) {
                            Text("Ouvrir et exporter mon carnet")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { showConfirmDeleteAll = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_delete_local_data_button")
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Effacer mes progressions et notes locales")
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAll = false },
            title = { Text("Effacer les données locales ?") },
            text = {
                Text("Vos progressions d'étude et notes personnelles stockées sur cet appareil seront définitivement supprimées.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllUserProgress()
                        showConfirmDeleteAll = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Effacer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteAll = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

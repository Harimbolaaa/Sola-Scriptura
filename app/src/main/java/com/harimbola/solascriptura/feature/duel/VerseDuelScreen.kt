package com.harimbola.solascriptura.feature.duel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.DiscourseLayer
import com.harimbola.solascriptura.core.designsystem.DiscourseLayerPill
import com.harimbola.solascriptura.core.designsystem.EmptyStateView
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.core.engine.EngineVerseReference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerseDuelScreen(
    onBack: () -> Unit,
    viewModel: VerseDuelViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.savedBanner) {
        state.savedBanner?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearBanner()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.duel_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("duel_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        val duel = state.currentDuel
        if (duel == null) {
            EmptyStateView(
                title = stringResource(R.string.duel_title),
                description = "Aucun duel de versets disponible pour cette étude.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val isWide = maxWidth >= 600.dp
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 900.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(dims.screenPadding),
                verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
            ) {
                if (state.duels.size > 1) {
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            itemsIndexed(state.duels) { idx, item ->
                                FilterChip(
                                    selected = idx == state.selectedIndex,
                                    onClick = { viewModel.selectDuel(idx) },
                                    label = { Text(item.title) },
                                    modifier = Modifier.testTag("duel_selector_$idx")
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = duel.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                item {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DuelPassageCard(
                                label = stringResource(R.string.duel_passage_a),
                                passage = duel.passageA,
                                context = duel.contextA,
                                modifier = Modifier.weight(1f)
                            )
                            DuelPassageCard(
                                label = stringResource(R.string.duel_passage_b),
                                passage = duel.passageB,
                                context = duel.contextB,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            DuelPassageCard(
                                label = stringResource(R.string.duel_passage_a),
                                passage = duel.passageA,
                                context = duel.contextA
                            )
                            DuelPassageCard(
                                label = stringResource(R.string.duel_passage_b),
                                passage = duel.passageB,
                                context = duel.contextB
                            )
                        }
                    }
                }

                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        DiscourseLayerPill(DiscourseLayer.PERSONAL_OPINION)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.duel_analysis_prompt),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = duel.analysisQuestion,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.reflectionText,
                            onValueChange = viewModel::updateReflection,
                            placeholder = { Text(stringResource(R.string.duel_reflection_hint)) },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("duel_reflection_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (duel.suggestedSynthesis.isNotBlank()) {
                            OutlinedButton(
                                onClick = { viewModel.toggleSuggestedSynthesis() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("duel_toggle_synthesis")
                            ) {
                                Text(
                                    if (state.showSuggestedSynthesis) "Masquer la piste SDA" else "Voir une piste d'interprétation (SDA)"
                                )
                            }
                            AnimatedVisibility(visible = state.showSuggestedSynthesis) {
                                SolaSurfaceCard(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    DiscourseLayerPill(DiscourseLayer.PROPOSED_INTERPRETATION)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = duel.suggestedSynthesis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                        Button(
                            onClick = { viewModel.saveComparisonToNotebook() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("duel_save_button")
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.duel_save_synthesis))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuelPassageCard(
    label: String,
    passage: EngineVerseReference,
    context: String,
    modifier: Modifier = Modifier
) {
    SolaSurfaceCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${passage.formattedReference()} (${passage.translationId})",
            style = MaterialTheme.typography.titleSmall
        )
        if (passage.quotedText.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "« ${passage.quotedText} »",
                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
            )
        }
        if (context.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = context,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

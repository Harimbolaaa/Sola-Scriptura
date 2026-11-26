package com.harimbola.solascriptura.feature.investigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestigationScreen(
    onBack: () -> Unit,
    viewModel: InvestigationViewModel = hiltViewModel()
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
                title = { Text(stringResource(R.string.investigation_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("investigation_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        val inv = state.investigation
        if (inv == null) {
            EmptyStateView(
                title = stringResource(R.string.investigation_title),
                description = "Aucun dossier d'enquête disponible pour cette étude.",
                modifier = Modifier.padding(innerPadding)
            )
            return@Scaffold
        }

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
                verticalArrangement = Arrangement.spacedBy(dims.itemSpacing)
            ) {
                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = stringResource(R.string.investigation_central_question),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = inv.centralQuestion,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                item {
                    Text(
                        text = stringResource(R.string.investigation_clues),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(inv.clues, key = { it.id }) { clue ->
                    val expanded = clue.id in state.revealedClueIds
                    SolaSurfaceCard(
                        onClick = { viewModel.toggleClueRevealed(clue.id) },
                        modifier = Modifier.testTag("investigation_clue_${clue.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = clue.testamentSection,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = clue.verseReference.formattedReference(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = null
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = clue.observationPrompt,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        AnimatedVisibility(visible = expanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                if (clue.verseReference.quotedText.isNotBlank()) {
                                    DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "« ${clue.verseReference.quotedText} »",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = clue.keyInsight,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
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
                            text = inv.synthesisPrompt,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.argumentDraft,
                            onValueChange = viewModel::updateArgument,
                            placeholder = { Text(stringResource(R.string.investigation_argument_hint)) },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("investigation_argument_input")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.saveInvestigation() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("investigation_save_button")
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.investigation_save))
                        }
                    }
                }
            }
        }
    }
}

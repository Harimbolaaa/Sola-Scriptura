package com.harimbola.solascriptura.feature.dailychallenge

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
fun DailyChallengeScreen(
    onBack: () -> Unit,
    onOpenStudy: (String) -> Unit,
    viewModel: DailyChallengeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.daily_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("daily_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        val challenge = state.challenge
        if (challenge == null) {
            EmptyStateView(
                title = stringResource(R.string.daily_title),
                description = stringResource(R.string.studies_empty),
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
                verticalArrangement = Arrangement.spacedBy(dims.sectionSpacing)
            ) {
                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = MaterialTheme.shapes.extraSmall,
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = stringResource(R.string.daily_offline_badge),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = challenge.dateIso,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = challenge.studyTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = challenge.promptText,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }

                items(challenge.verses, key = { "${it.bookId}_${it.chapter}_${it.startVerse}" }) { verse ->
                    SolaSurfaceCard {
                        DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${verse.formattedReference()} (${verse.translationId})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (verse.quotedText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "« ${verse.quotedText} »",
                                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic)
                            )
                        }
                    }
                }

                item {
                    SolaSurfaceCard {
                        DiscourseLayerPill(DiscourseLayer.PERSONAL_OPINION)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.reflectionDraft,
                            onValueChange = viewModel::updateReflection,
                            placeholder = { Text(stringResource(R.string.dialogue_personal_conclusion_hint)) },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("daily_reflection_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.markCompleted() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("daily_mark_done_button")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (challenge.isCompletedToday) {
                                    "Méditation enregistrée aujourd'hui"
                                } else {
                                    stringResource(R.string.daily_mark_done)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onOpenStudy(challenge.studyId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("daily_open_study_button")
                        ) {
                            Text(stringResource(R.string.truth_tree_open_study))
                        }
                    }
                }
            }
        }
    }
}

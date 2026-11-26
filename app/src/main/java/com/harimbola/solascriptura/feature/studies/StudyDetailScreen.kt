package com.harimbola.solascriptura.feature.studies

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
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ManageSearch
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.DiscourseLayer
import com.harimbola.solascriptura.core.designsystem.DiscourseLayerPill
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyDetailScreen(
    studyId: String,
    onBack: () -> Unit,
    onStartDialogue: (String) -> Unit,
    onOpenVerseDuel: (String) -> Unit,
    onOpenInvestigation: (String) -> Unit,
    onOpenRelatedStudy: (String) -> Unit,
    viewModel: StudiesViewModel = hiltViewModel()
) {
    val detailFlow = remember(studyId) { viewModel.observeStudyDetail(studyId) }
    val state by detailFlow.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val study = state.study

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(study?.title ?: "") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("study_detail_back")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (study != null) {
                        IconButton(
                            onClick = { viewModel.toggleFavorite(study.studyId) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("study_detail_favorite")
                        ) {
                            Icon(
                                imageVector = if (state.isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Favori"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (study == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.studies_empty))
            }
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            EditorialBadge(study.editorialStatus)
                            Text(
                                text = stringResource(R.string.studies_minutes_format, study.estimatedMinutes),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = study.title,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = study.subtitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = study.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Methodological Clarity Card
                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ) {
                        Text(
                            text = stringResource(R.string.studies_detail_distinction_title),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.studies_detail_distinction_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
                            DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DiscourseLayerPill(DiscourseLayer.PROPOSED_INTERPRETATION)
                            DiscourseLayerPill(DiscourseLayer.PERSONAL_OPINION)
                        }
                    }
                }

                // Actions: Start / Resume / Restart + Duel + Investigation
                item {
                    val hasProgress = state.progress != null
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onStartDialogue(study.studyId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("study_detail_start_dialogue")
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasProgress && state.progress?.isCompleted == false) {
                                    stringResource(R.string.studies_detail_resume)
                                } else {
                                    stringResource(R.string.studies_detail_start)
                                }
                            )
                        }

                        if (hasProgress) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.restartStudy(study.studyId)
                                    onStartDialogue(study.studyId)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("study_detail_restart")
                            ) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.studies_detail_restart))
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onOpenVerseDuel(study.studyId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("study_detail_duel")
                            ) {
                                Icon(Icons.Rounded.Balance, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.studies_detail_duel))
                            }
                            OutlinedButton(
                                onClick = { onOpenInvestigation(study.studyId) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("study_detail_investigation")
                            ) {
                                Icon(Icons.Rounded.ManageSearch, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.studies_detail_investigation))
                            }
                        }
                    }
                }

                if (state.relatedStudies.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.dialogue_next_study),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    items(state.relatedStudies, key = { it.studyId }) { related ->
                        SolaSurfaceCard(
                            onClick = { onOpenRelatedStudy(related.studyId) },
                            modifier = Modifier.testTag("related_study_${related.studyId}")
                        ) {
                            Text(related.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                related.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

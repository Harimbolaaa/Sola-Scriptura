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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.OfflinePin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.EmptyStateView
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress

@Composable
fun StudiesScreen(
    initialCategoryId: String? = null,
    onOpenStudyDetail: (String) -> Unit,
    viewModel: StudiesViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current

    LaunchedEffect(initialCategoryId) {
        if (initialCategoryId != null) {
            viewModel.selectCategory(initialCategoryId)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = dims.maxContentWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = dims.screenPadding,
                vertical = dims.screenPadding
            ),
            verticalArrangement = Arrangement.spacedBy(dims.itemSpacing)
        ) {
            item {
                Text(
                    text = stringResource(R.string.studies_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    placeholder = { Text(stringResource(R.string.studies_search_hint)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.updateSearchQuery("") },
                                modifier = Modifier.testTag("studies_clear_search")
                            ) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("studies_search_field")
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text(stringResource(R.string.studies_all_seasons)) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("studies_filter_all")
                        )
                    }
                    items(state.categories, key = { it.categoryId }) { cat ->
                        FilterChip(
                            selected = state.selectedCategoryId == cat.categoryId,
                            onClick = { viewModel.selectCategory(cat.categoryId) },
                            label = { Text(cat.title) },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("studies_filter_${cat.categoryId}")
                        )
                    }
                }
            }

            if (state.studies.isEmpty()) {
                item {
                    EmptyStateView(
                        title = stringResource(R.string.studies_title),
                        description = stringResource(R.string.studies_empty)
                    )
                }
            } else {
                items(state.studies, key = { "${it.studyId}_${it.locale}" }) { study ->
                    StudyLibraryCard(
                        study = study,
                        progress = state.progressByStudyId[study.studyId],
                        isFavorite = study.studyId in state.favoriteIds,
                        onClick = { onOpenStudyDetail(study.studyId) },
                        onToggleFavorite = { viewModel.toggleFavorite(study.studyId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudyLibraryCard(
    study: Study,
    progress: StudyProgress?,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    SolaSurfaceCard(
        onClick = onClick,
        modifier = Modifier.testTag("study_card_${study.studyId}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditorialBadge(editorialStatus = study.editorialStatus)
                    if (progress?.isCompleted == true) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Terminée",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = study.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = study.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("study_favorite_${study.studyId}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                    contentDescription = "Favori",
                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.studies_minutes_format, study.estimatedMinutes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.OfflinePin,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.studies_offline_ready),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Text(
                text = stringResource(R.string.studies_perspective_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditorialBadge(editorialStatus: String) {
    val isReviewed = editorialStatus.equals("REVIEWED", ignoreCase = true)
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = if (isReviewed) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.tertiaryContainer
        }
    ) {
        Text(
            text = if (isReviewed) {
                stringResource(R.string.studies_reviewed_badge)
            } else {
                stringResource(R.string.studies_demo_badge)
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isReviewed) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onTertiaryContainer
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

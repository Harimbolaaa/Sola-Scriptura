package com.harimbola.solascriptura.feature.notebook

import android.content.Intent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.UserNote

@Composable
fun NotebookScreen(
    onOpenStudyDetail: (String) -> Unit,
    viewModel: NotebookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val context = LocalContext.current

    var editingNote by remember { mutableStateOf<UserNote?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.exportedMarkdown) {
        val md = state.exportedMarkdown
        if (md != null) {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_SUBJECT, "Carnet Sola Scriptura")
                putExtra(Intent.EXTRA_TEXT, md)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Exporter le carnet"))
            viewModel.consumeExport()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.testTag("notebook_add_fab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.notebook_add_note))
            }
        }
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
                verticalArrangement = Arrangement.spacedBy(dims.itemSpacing)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.notebook_title),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        TextButton(
                            onClick = { viewModel.prepareExport() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("notebook_export_button")
                        ) {
                            Icon(Icons.Rounded.IosShare, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.notebook_export))
                        }
                    }
                }

                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val filters = listOf(
                            NotebookFilter.ALL to R.string.notebook_type_all,
                            NotebookFilter.CONCLUSION to R.string.notebook_type_conclusion,
                            NotebookFilter.QUESTION to R.string.notebook_type_question,
                            NotebookFilter.NOTE to R.string.notebook_type_note,
                            NotebookFilter.VERSE to R.string.notebook_type_verse,
                            NotebookFilter.DIFFICULTY to R.string.notebook_type_difficulty,
                            NotebookFilter.FAVORITES to R.string.notebook_type_favorite
                        )
                        items(filters, key = { it.first.name }) { (filter, labelRes) ->
                            FilterChip(
                                selected = state.filter == filter,
                                onClick = { viewModel.setFilter(filter) },
                                label = { Text(stringResource(labelRes)) },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("notebook_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                }

                if (state.filter == NotebookFilter.FAVORITES) {
                    if (state.favoriteStudies.isEmpty()) {
                        item { NotebookEmptyIllustration() }
                    } else {
                        items(state.favoriteStudies, key = { it.studyId }) { study ->
                            SolaSurfaceCard(
                                onClick = { onOpenStudyDetail(study.studyId) },
                                modifier = Modifier.testTag("notebook_favorite_${study.studyId}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(study.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            study.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(study.studyId) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Rounded.BookmarkRemove, contentDescription = "Retirer")
                                    }
                                }
                            }
                        }
                    }
                } else if (state.notes.isEmpty()) {
                    item { NotebookEmptyIllustration() }
                } else {
                    items(state.notes, key = { it.id }) { note ->
                        SolaSurfaceCard(
                            modifier = Modifier.testTag("notebook_note_${note.id}")
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
                                        text = note.noteType.labelFr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Row {
                                    IconButton(
                                        onClick = { editingNote = note },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("notebook_edit_${note.id}")
                                    ) {
                                        Icon(Icons.Rounded.Edit, contentDescription = "Modifier")
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteNote(note.id) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("notebook_delete_${note.id}")
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Supprimer")
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(note.title, style = MaterialTheme.typography.titleMedium)
                            note.verseRefLabel?.takeIf { it.isNotBlank() }?.let { ref ->
                                Text(
                                    text = ref,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog || editingNote != null) {
        val current = editingNote
        NoteEditorDialog(
            initialNote = current,
            defaultStudyId = state.studiesMap.keys.firstOrNull() ?: "general",
            onDismiss = {
                showCreateDialog = false
                editingNote = null
            },
            onSave = { title, content, type, verse ->
                viewModel.saveNote(
                    id = current?.id,
                    studyId = current?.studyId ?: (state.studiesMap.keys.firstOrNull() ?: "general"),
                    noteType = type,
                    title = title.ifBlank { type.labelFr },
                    content = content,
                    verseRefLabel = verse
                )
                showCreateDialog = false
                editingNote = null
            }
        )
    }
}

@Composable
private fun NotebookEmptyIllustration() {
    SolaSurfaceCard(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_empty_notebook),
            contentDescription = stringResource(R.string.notebook_empty_title),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(MaterialTheme.shapes.small)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.notebook_empty_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.notebook_empty_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NoteEditorDialog(
    initialNote: UserNote?,
    defaultStudyId: String,
    onDismiss: () -> Unit,
    onSave: (String, String, NoteType, String?) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var verse by remember { mutableStateOf(initialNote?.verseRefLabel ?: "") }
    var selectedType by remember { mutableStateOf(initialNote?.noteType ?: NoteType.NOTE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialNote == null) stringResource(R.string.notebook_add_note) else "Modifier la note") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(NoteType.entries) { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.labelFr) }
                        )
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notebook_dialog_title")
                )
                OutlinedTextField(
                    value = verse,
                    onValueChange = { verse = it },
                    label = { Text("Passage biblique (optionnel)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notebook_dialog_verse")
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Contenu") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("notebook_dialog_content")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedType, verse.ifBlank { null }) },
                modifier = Modifier.testTag("notebook_dialog_save")
            ) {
                Text(stringResource(R.string.dialogue_save_later))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

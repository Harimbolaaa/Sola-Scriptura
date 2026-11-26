package com.harimbola.solascriptura.feature.dialogue

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.harimbola.solascriptura.core.designsystem.LocalAppDimensions
import com.harimbola.solascriptura.core.designsystem.LocalStudyColors
import com.harimbola.solascriptura.core.designsystem.SolaSurfaceCard
import com.harimbola.solascriptura.core.engine.ChoiceKind
import com.harimbola.solascriptura.core.engine.DialogueNodeType
import com.harimbola.solascriptura.core.engine.DialoguePhase
import com.harimbola.solascriptura.core.engine.EngineVerseReference
import com.harimbola.solascriptura.domain.model.NoteType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogueScreen(
    onBack: () -> Unit,
    onOpenRelatedStudy: (String) -> Unit,
    viewModel: DialogueViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dims = LocalAppDimensions.current
    val semantic = LocalStudyColors.current
    val snackbarHostState = remember { SnackbarHostState() }

    var showContextSheet by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showQuickNoteDialog by remember { mutableStateOf(false) }
    var showInterpretationExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.savedConfirmationMessage) {
        val msg = state.savedConfirmationMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearConfirmationMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.study?.title ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                        val phaseText = when (state.currentNode?.phase) {
                            DialoguePhase.IRONY -> stringResource(R.string.dialogue_phase_irony)
                            DialoguePhase.ELENCHUS -> stringResource(R.string.dialogue_phase_elenchus)
                            DialoguePhase.MAIEUTICS -> stringResource(R.string.dialogue_phase_maieutics)
                            DialoguePhase.SYNTHESIS -> stringResource(R.string.dialogue_phase_synthesis)
                            DialoguePhase.REFLECTION -> stringResource(R.string.dialogue_phase_reflection)
                            null -> ""
                        }
                        if (phaseText.isNotEmpty()) {
                            Text(
                                text = phaseText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("dialogue_quit_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.dialogue_quit)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dialogue_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.History,
                            contentDescription = stringResource(R.string.dialogue_history)
                        )
                    }
                    IconButton(
                        onClick = { showQuickNoteDialog = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dialogue_quick_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = stringResource(R.string.notebook_add_note)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.saveSessionForLater() },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dialogue_save_later_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Save,
                            contentDescription = stringResource(R.string.dialogue_save_later)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val node = state.currentNode
        val session = state.session
        if (node == null || session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.error_generic))
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
                verticalArrangement = Arrangement.spacedBy(dims.itemSpacing)
            ) {
                // Progress bar
                item {
                    LinearProgressIndicator(
                        progress = { state.progressFraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(MaterialTheme.shapes.extraSmall)
                            .testTag("dialogue_progress_bar")
                    )
                }

                // Feedback banner from previous choice
                state.feedbackBanner?.let { feedback ->
                    item {
                        SolaSurfaceCard(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                            modifier = Modifier.testTag("dialogue_feedback_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = feedback,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }

                // Main Socratic Question Bubble
                item {
                    SolaSurfaceCard(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.testTag("dialogue_speaker_card")
                    ) {
                        Text(
                            text = node.speakerText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (node.instructionalText.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = node.instructionalText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Biblical Text Cards (Layer 1: BIBLICAL_TEXT)
                val allVerses = (node.verseReferences + state.extraVerses)
                    .distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}" }
                if (state.showVerseReferences && allVerses.isNotEmpty()) {
                    items(allVerses, key = { "${it.bookId}_${it.chapter}_${it.startVerse}" }) { verse ->
                        BiblicalVerseCard(
                            verse = verse,
                            showContextSummary = state.showBiblicalContext
                        )
                    }
                }

                // Historical Context (Layer 2: HISTORICAL_CONTEXT)
                if (state.showBiblicalContext && node.contextText.isNotBlank()) {
                    item {
                        SolaSurfaceCard(
                            onClick = { showContextSheet = true },
                            containerColor = semantic.contextLayerAccent.copy(alpha = 0.08f),
                            borderColor = semantic.contextLayerAccent.copy(alpha = 0.35f),
                            modifier = Modifier.testTag("dialogue_context_card")
                        ) {
                            DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = node.contextText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.dialogue_context_button),
                                style = MaterialTheme.typography.labelMedium,
                                color = semantic.contextLayerAccent
                            )
                        }
                    }
                }

                // Proposed SDA Interpretation (Layer 3: PROPOSED_INTERPRETATION, expandable & clearly labeled)
                if (node.proposedInterpretation.isNotBlank()) {
                    item {
                        SolaSurfaceCard(
                            onClick = { showInterpretationExpanded = !showInterpretationExpanded },
                            containerColor = semantic.interpretationLayerAccent.copy(alpha = 0.07f),
                            borderColor = semantic.interpretationLayerAccent.copy(alpha = 0.35f),
                            modifier = Modifier.testTag("dialogue_interpretation_card")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                DiscourseLayerPill(DiscourseLayer.PROPOSED_INTERPRETATION)
                                Icon(
                                    imageVector = if (showInterpretationExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = semantic.interpretationLayerAccent
                                )
                            }
                            AnimatedVisibility(visible = showInterpretationExpanded) {
                                Column {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = node.proposedInterpretation,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Choices
                if (node.choices.isNotEmpty()) {
                    items(node.choices, key = { it.id }) { choice ->
                        val isSecondaryKind = choice.kind != ChoiceKind.STANDARD
                        if (isSecondaryKind) {
                            OutlinedButton(
                                onClick = { viewModel.selectChoice(choice.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("dialogue_choice_${choice.id}"),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = choice.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = { viewModel.selectChoice(choice.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("dialogue_choice_${choice.id}"),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(
                                    text = choice.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Back button row when back navigation is available
                if (node.allowBackNavigation && session.history.steps.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            TextButton(
                                onClick = { viewModel.goBack() },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("dialogue_back_step_button")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.dialogue_back))
                            }
                            if (node.contextText.isNotBlank() || allVerses.isNotEmpty()) {
                                TextButton(
                                    onClick = { showContextSheet = true },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("dialogue_open_context_sheet")
                                ) {
                                    Icon(Icons.Rounded.MenuBook, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.dialogue_context_button))
                                }
                            }
                        }
                    }
                }

                // Synthesis / Terminal Conclusion Section (Layer 4: PERSONAL_OPINION)
                val conclusion = state.conclusion
                if (conclusion != null || node.terminal || node.type == DialogueNodeType.SYNTHESIS || node.type == DialogueNodeType.END) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (conclusion != null) {
                                SolaSurfaceCard(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    modifier = Modifier.testTag("dialogue_conclusion_card")
                                ) {
                                    DiscourseLayerPill(DiscourseLayer.PROPOSED_INTERPRETATION)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = conclusion.title,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = conclusion.summary,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    if (conclusion.reasoningSteps.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        conclusion.reasoningSteps.forEachIndexed { idx, step ->
                                            Text(
                                                text = "${idx + 1}. $step",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Personal Opinion & Conclusion Input
                            SolaSurfaceCard(
                                containerColor = semantic.personalLayerAccent.copy(alpha = 0.08f),
                                borderColor = semantic.personalLayerAccent.copy(alpha = 0.35f),
                                modifier = Modifier.testTag("dialogue_personal_opinion_card")
                            ) {
                                DiscourseLayerPill(DiscourseLayer.PERSONAL_OPINION)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (conclusion?.reflectionQuestion?.isNotBlank() == true) {
                                    Text(
                                        text = conclusion.reflectionQuestion,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                OutlinedTextField(
                                    value = state.personalConclusionDraft,
                                    onValueChange = viewModel::updatePersonalConclusionDraft,
                                    placeholder = {
                                        Text(stringResource(R.string.dialogue_personal_conclusion_hint))
                                    },
                                    minLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("dialogue_personal_conclusion_input")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(R.string.dialogue_mark_unconvinced),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = state.remainsUnconvincedDraft,
                                        onCheckedChange = viewModel::toggleRemainsUnconvinced,
                                        modifier = Modifier.testTag("dialogue_unconvinced_switch")
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.saveConclusionToNotebook() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("dialogue_save_notebook_button")
                                    ) {
                                        Icon(Icons.Rounded.BookmarkAdd, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.dialogue_save_conclusion))
                                    }
                                    Button(
                                        onClick = { viewModel.completeStudy() },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .testTag("dialogue_complete_study_button")
                                    ) {
                                        Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(stringResource(R.string.dialogue_complete_study))
                                    }
                                }
                            }

                            conclusion?.relatedStudyIds?.forEach { relatedId ->
                                OutlinedButton(
                                    onClick = { onOpenRelatedStudy(relatedId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("dialogue_next_study_$relatedId")
                                ) {
                                    Text("${stringResource(R.string.dialogue_next_study)} ($relatedId)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showContextSheet) {
        val node = state.currentNode
        ModalBottomSheet(onDismissRequest = { showContextSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DiscourseLayerPill(DiscourseLayer.HISTORICAL_CONTEXT)
                Text(
                    text = node?.contextText?.ifBlank {
                        "Observe le genre littéraire, les interlocuteurs et l'ensemble du chapitre biblique."
                    } ?: "",
                    style = MaterialTheme.typography.bodyLarge
                )
                node?.verseReferences?.filter { it.contextSummary.isNotBlank() }?.forEach { v ->
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = v.formattedReference(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = v.contextSummary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showHistorySheet) {
        val historySteps = state.session?.history?.steps.orEmpty()
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.dialogue_history),
                    style = MaterialTheme.typography.titleLarge
                )
                if (historySteps.isEmpty()) {
                    Text(
                        text = "Ton cheminement s'affichera ici au fil des questions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    historySteps.forEachIndexed { idx, step ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "${idx + 1}. ${step.choiceLabel ?: step.questionText}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Nœud : ${step.nodeId} (${step.phaseName})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showQuickNoteDialog) {
        QuickNoteDialog(
            defaultVerseRef = state.currentNode?.verseReferences?.firstOrNull()?.formattedReference(),
            onDismiss = { showQuickNoteDialog = false },
            onSave = { title, content, type, ref ->
                viewModel.saveQuickNodeNote(title, content, type, ref)
                showQuickNoteDialog = false
            }
        )
    }
}

@Composable
private fun BiblicalVerseCard(
    verse: EngineVerseReference,
    showContextSummary: Boolean
) {
    val semantic = LocalStudyColors.current
    SolaSurfaceCard(
        containerColor = semantic.bibleLayerAccent.copy(alpha = 0.08f),
        borderColor = semantic.bibleLayerAccent.copy(alpha = 0.35f),
        modifier = Modifier.testTag("dialogue_verse_${verse.bookId}_${verse.chapter}_${verse.startVerse}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DiscourseLayerPill(DiscourseLayer.BIBLICAL_TEXT)
            Text(
                text = "${verse.formattedReference()} (${verse.translationId})",
                style = MaterialTheme.typography.labelMedium,
                color = semantic.bibleLayerAccent,
                fontWeight = FontWeight.Bold
            )
        }
        if (verse.quotedText.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "« ${verse.quotedText} »",
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (showContextSummary && verse.contextSummary.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = verse.contextSummary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickNoteDialog(
    defaultVerseRef: String?,
    onDismiss: () -> Unit,
    onSave: (String, String, NoteType, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NoteType.NOTE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.notebook_add_note)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(NoteType.NOTE, NoteType.QUESTION, NoteType.DIFFICULTY).forEach { type ->
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
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Réflexion ou question") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, content, selectedType, defaultVerseRef) }) {
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

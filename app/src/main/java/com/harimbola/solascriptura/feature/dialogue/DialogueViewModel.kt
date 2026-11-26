package com.harimbola.solascriptura.feature.dialogue

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.core.engine.DialogueConclusion
import com.harimbola.solascriptura.core.engine.DialogueEngine
import com.harimbola.solascriptura.core.engine.DialogueGraph
import com.harimbola.solascriptura.core.engine.DialogueNode
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.engine.DialogueResult
import com.harimbola.solascriptura.core.engine.DialogueSession
import com.harimbola.solascriptura.core.engine.DialogueTransition
import com.harimbola.solascriptura.core.engine.EngineVerseReference
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DialogueUiState(
    val isLoading: Boolean = true,
    val study: Study? = null,
    val currentNode: DialogueNode? = null,
    val session: DialogueSession? = null,
    val conclusion: DialogueConclusion? = null,
    val result: DialogueResult? = null,
    val progressFraction: Float = 0f,
    val feedbackBanner: String? = null,
    val extraVerses: List<EngineVerseReference> = emptyList(),
    val showVerseReferences: Boolean = true,
    val showBiblicalContext: Boolean = true,
    val personalConclusionDraft: String = "",
    val remainsUnconvincedDraft: Boolean = false,
    val savedConfirmationMessage: String? = null
)

@HiltViewModel
class DialogueViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studyRepository: StudyRepository,
    private val progressRepository: ProgressRepository,
    private val notebookRepository: NotebookRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dialogueEngine: DialogueEngine,
    private val progressManager: DialogueProgressManager
) : ViewModel() {

    private val studyId: String = savedStateHandle.get<String>("studyId") ?: ""

    private val _uiState = MutableStateFlow(DialogueUiState())
    val uiState: StateFlow<DialogueUiState> = _uiState.asStateFlow()

    private var activeGraph: DialogueGraph? = null

    init {
        loadStudy()
    }

    private fun loadStudy() {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferences.first()
            val study = studyRepository.getStudyById(studyId, prefs.study.language)
            if (study == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            activeGraph = study.graph
            val existingProgress = progressRepository.observeProgressForStudy(study.studyId).first()

            val session = if (existingProgress != null) {
                progressManager.restoreSession(
                    graph = study.graph,
                    currentNodeId = existingProgress.currentNodeId,
                    historyJson = existingProgress.historyJson,
                    answersJson = existingProgress.answersJson,
                    notionsJson = progressManager.encodeNotions(existingProgress.unlockedNotions),
                    personalConclusion = existingProgress.personalConclusion,
                    isUnconvinced = existingProgress.isUnconvinced,
                    isCompleted = existingProgress.isCompleted,
                    startedAtMillis = existingProgress.startedAt,
                    updatedAtMillis = existingProgress.updatedAt
                )
            } else {
                dialogueEngine.startSession(study.graph)
            }

            progressRepository.recordNodeVisit(study.studyId, session.currentNodeId)
            persistSession(session, enqueueSync = false)
            publishState(study, study.graph, session, prefs.study.showVerseReferences, prefs.study.showBiblicalContext)
        }
    }

    fun selectChoice(choiceId: String) {
        val graph = activeGraph ?: return
        val study = _uiState.value.study ?: return
        val currentSession = _uiState.value.session ?: return

        val (updatedSession, transition) = dialogueEngine.selectChoice(graph, currentSession, choiceId)
        val feedback = when (transition) {
            is DialogueTransition.MoveToNode -> transition.feedback.ifBlank { null }
            is DialogueTransition.StayWithFeedback -> transition.feedback.ifBlank { null }
            is DialogueTransition.CycleOrLimitFallback -> transition.reason
            is DialogueTransition.GoBack -> null
            is DialogueTransition.Complete -> updatedSession.lastFeedback
        }
        val extraVerses = if (transition is DialogueTransition.StayWithFeedback) {
            transition.extraVerses
        } else {
            emptyList()
        }

        viewModelScope.launch {
            progressRepository.recordNodeVisit(study.studyId, updatedSession.currentNodeId)
            persistSession(updatedSession, enqueueSync = true)
            publishState(
                study = study,
                graph = graph,
                session = updatedSession,
                showRefs = _uiState.value.showVerseReferences,
                showContext = _uiState.value.showBiblicalContext,
                feedback = feedback,
                extraVerses = extraVerses
            )
        }
    }

    fun goBack() {
        val graph = activeGraph ?: return
        val study = _uiState.value.study ?: return
        val currentSession = _uiState.value.session ?: return
        val (restored, _) = dialogueEngine.goBack(graph, currentSession)
        viewModelScope.launch {
            persistSession(restored, enqueueSync = false)
            publishState(
                study = study,
                graph = graph,
                session = restored,
                showRefs = _uiState.value.showVerseReferences,
                showContext = _uiState.value.showBiblicalContext
            )
        }
    }

    fun updatePersonalConclusionDraft(text: String) {
        _uiState.update { it.copy(personalConclusionDraft = text) }
    }

    fun toggleRemainsUnconvinced(unconvinced: Boolean) {
        _uiState.update { it.copy(remainsUnconvincedDraft = unconvinced) }
    }

    fun saveSessionForLater() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val updated = session.copy(
                personalConclusion = _uiState.value.personalConclusionDraft,
                isUnconvinced = _uiState.value.remainsUnconvincedDraft,
                updatedAtMillis = System.currentTimeMillis()
            )
            persistSession(updated, enqueueSync = true)
            _uiState.update {
                it.copy(
                    session = updated,
                    savedConfirmationMessage = "Progression enregistrée hors ligne."
                )
            }
        }
    }

    fun saveConclusionToNotebook() {
        val study = _uiState.value.study ?: return
        val draft = _uiState.value.personalConclusionDraft.trim()
        val conclusionTitle = _uiState.value.conclusion?.title ?: study.title
        val textToSave = draft.ifBlank {
            _uiState.value.conclusion?.summary ?: return
        }
        viewModelScope.launch {
            notebookRepository.saveNote(
                studyId = study.studyId,
                nodeId = _uiState.value.currentNode?.id,
                noteType = if (_uiState.value.remainsUnconvincedDraft) NoteType.QUESTION else NoteType.CONCLUSION,
                title = conclusionTitle,
                content = textToSave,
                verseRefLabel = _uiState.value.result?.examinedVerses
                    ?.joinToString(", ") { it.formattedReference() }
            )
            _uiState.update {
                it.copy(savedConfirmationMessage = "Enregistré dans votre carnet de réflexion.")
            }
        }
    }

    fun saveQuickNodeNote(title: String, content: String, noteType: NoteType, verseRef: String?) {
        val study = _uiState.value.study ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            notebookRepository.saveNote(
                studyId = study.studyId,
                nodeId = _uiState.value.currentNode?.id,
                noteType = noteType,
                title = title.ifBlank { study.title },
                content = content,
                verseRefLabel = verseRef
            )
            _uiState.update {
                it.copy(savedConfirmationMessage = "Note ajoutée au carnet.")
            }
        }
    }

    fun completeStudy() {
        val graph = activeGraph ?: return
        val study = _uiState.value.study ?: return
        val currentSession = _uiState.value.session ?: return
        val (completedSession, result) = dialogueEngine.completeWithReflection(
            graph = graph,
            session = currentSession,
            personalConclusion = _uiState.value.personalConclusionDraft,
            remainsUnconvinced = _uiState.value.remainsUnconvincedDraft
        )
        viewModelScope.launch {
            persistSession(completedSession, enqueueSync = true)
            publishState(
                study = study,
                graph = graph,
                session = completedSession,
                showRefs = _uiState.value.showVerseReferences,
                showContext = _uiState.value.showBiblicalContext,
                resultOverride = result
            )
            _uiState.update {
                it.copy(savedConfirmationMessage = "Étude terminée et enregistrée.")
            }
        }
    }

    fun clearConfirmationMessage() {
        _uiState.update { it.copy(savedConfirmationMessage = null) }
    }

    private suspend fun persistSession(session: DialogueSession, enqueueSync: Boolean) {
        val now = System.currentTimeMillis()
        val progress = StudyProgress(
            studyId = session.studyId,
            currentNodeId = session.currentNodeId,
            historyJson = progressManager.encodeHistory(session.history),
            answersJson = progressManager.encodeAnswers(session.selectedAnswers),
            unlockedNotions = session.unlockedNotions,
            personalConclusion = session.personalConclusion,
            isUnconvinced = session.isUnconvinced,
            isCompleted = session.isCompleted,
            startedAt = session.startedAtMillis.takeIf { it > 0L } ?: now,
            lastOpenedAt = now,
            completedAt = if (session.isCompleted) now else null,
            updatedAt = now
        )
        progressRepository.saveProgress(progress, enqueueSync = enqueueSync)
    }

    private fun publishState(
        study: Study,
        graph: DialogueGraph,
        session: DialogueSession,
        showRefs: Boolean,
        showContext: Boolean,
        feedback: String? = session.lastFeedback,
        extraVerses: List<EngineVerseReference> = emptyList(),
        resultOverride: DialogueResult? = null
    ) {
        val node = dialogueEngine.currentNode(graph, session)
        val conclusion = graph.conclusionOrNull(node?.conclusionId)
        val result = resultOverride ?: dialogueEngine.buildResult(graph, session)
        _uiState.update {
            it.copy(
                isLoading = false,
                study = study,
                currentNode = node,
                session = session,
                conclusion = conclusion,
                result = result,
                progressFraction = dialogueEngine.progressFraction(graph, session),
                feedbackBanner = feedback,
                extraVerses = extraVerses,
                showVerseReferences = showRefs,
                showBiblicalContext = showContext,
                personalConclusionDraft = it.personalConclusionDraft.ifEmpty { session.personalConclusion },
                remainsUnconvincedDraft = session.isUnconvinced
            )
        }
    }

}

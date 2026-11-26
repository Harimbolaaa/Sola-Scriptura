package com.harimbola.solascriptura.feature.investigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.BibleInvestigation
import com.harimbola.solascriptura.domain.model.InvestigationClue
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestigationUiState(
    val study: Study? = null,
    val investigation: BibleInvestigation? = null,
    val revealedClueIds: Set<String> = emptySet(),
    val argumentDraft: String = "",
    val savedBanner: String? = null
)

@HiltViewModel
class InvestigationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studyRepository: StudyRepository,
    private val notebookRepository: NotebookRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val studyId: String = savedStateHandle.get<String>("studyId") ?: ""
    private val _uiState = MutableStateFlow(InvestigationUiState())
    val uiState: StateFlow<InvestigationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferences.first()
            val study = studyRepository.getStudyById(studyId, prefs.study.language)
            val investigation = study?.investigation ?: buildFallbackInvestigation(study)
            _uiState.update { it.copy(study = study, investigation = investigation) }
        }
    }

    private fun buildFallbackInvestigation(study: Study?): BibleInvestigation? {
        if (study == null) return null
        val refs = study.graph.nodes.values
            .flatMap { it.verseReferences }
            .distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}" }
        if (refs.isEmpty()) return null
        return BibleInvestigation(
            id = "${study.studyId}_auto_inv",
            centralQuestion = study.title,
            clues = refs.mapIndexed { idx, ref ->
                InvestigationClue(
                    id = "clue_$idx",
                    testamentSection = ref.bookName,
                    verseReference = ref,
                    observationPrompt = "Qu'apporte ce passage à la question centrale ?",
                    keyInsight = ref.contextSummary.ifBlank { "Observe les mots clés et le destinataire du texte." }
                )
            },
            synthesisPrompt = "Relie ces indices bibliques pour formuler ton raisonnement."
        )
    }

    fun toggleClueRevealed(clueId: String) {
        _uiState.update { state ->
            val updated = if (clueId in state.revealedClueIds) {
                state.revealedClueIds - clueId
            } else {
                state.revealedClueIds + clueId
            }
            state.copy(revealedClueIds = updated)
        }
    }

    fun updateArgument(text: String) {
        _uiState.update { it.copy(argumentDraft = text) }
    }

    fun saveInvestigation() {
        val state = _uiState.value
        val study = state.study ?: return
        val inv = state.investigation ?: return
        val content = state.argumentDraft.trim()
        if (content.isBlank()) return
        viewModelScope.launch {
            notebookRepository.saveNote(
                studyId = study.studyId,
                noteType = NoteType.CONCLUSION,
                title = "Enquête : ${inv.centralQuestion}",
                content = content,
                verseRefLabel = inv.clues.joinToString(", ") { it.verseReference.formattedReference() }
            )
            _uiState.update { it.copy(savedBanner = "Argumentation enregistrée dans le carnet.") }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(savedBanner = null) }
    }
}

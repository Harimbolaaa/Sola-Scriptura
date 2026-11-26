package com.harimbola.solascriptura.feature.duel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.VerseDuel
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

data class VerseDuelUiState(
    val study: Study? = null,
    val duels: List<VerseDuel> = emptyList(),
    val selectedIndex: Int = 0,
    val reflectionText: String = "",
    val showSuggestedSynthesis: Boolean = false,
    val savedBanner: String? = null
) {
    val currentDuel: VerseDuel? get() = duels.getOrNull(selectedIndex)
}

@HiltViewModel
class VerseDuelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studyRepository: StudyRepository,
    private val notebookRepository: NotebookRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val studyId: String = savedStateHandle.get<String>("studyId") ?: ""
    private val _uiState = MutableStateFlow(VerseDuelUiState())
    val uiState: StateFlow<VerseDuelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val prefs = preferencesRepository.userPreferences.first()
            val study = studyRepository.getStudyById(studyId, prefs.study.language)
            val duels = if (study?.verseDuels?.isNotEmpty() == true) {
                study.verseDuels
            } else {
                buildFallbackDuelsFromStudy(study)
            }
            _uiState.update { it.copy(study = study, duels = duels) }
        }
    }

    private fun buildFallbackDuelsFromStudy(study: Study?): List<VerseDuel> {
        if (study == null) return emptyList()
        val refs = study.graph.nodes.values
            .flatMap { it.verseReferences }
            .distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}" }
        if (refs.size < 2) return emptyList()
        return listOf(
            VerseDuel(
                id = "${study.studyId}_auto_duel",
                title = "Comparaison de passages — ${study.title}",
                passageA = refs[0],
                contextA = refs[0].contextSummary.ifBlank { "Premier passage examiné dans l'étude." },
                passageB = refs[1],
                contextB = refs[1].contextSummary.ifBlank { "Second passage examiné dans l'étude." },
                analysisQuestion = "Quel lien logique ou thématique observes-tu entre ces deux passages ?",
                suggestedSynthesis = study.graph.conclusions.values.firstOrNull()?.summary ?: ""
            )
        )
    }

    fun selectDuel(index: Int) {
        _uiState.update { it.copy(selectedIndex = index, showSuggestedSynthesis = false) }
    }

    fun updateReflection(text: String) {
        _uiState.update { it.copy(reflectionText = text) }
    }

    fun toggleSuggestedSynthesis() {
        _uiState.update { it.copy(showSuggestedSynthesis = !it.showSuggestedSynthesis) }
    }

    fun saveComparisonToNotebook() {
        val state = _uiState.value
        val duel = state.currentDuel ?: return
        val study = state.study ?: return
        val content = state.reflectionText.trim().ifBlank { duel.suggestedSynthesis }
        if (content.isBlank()) return
        viewModelScope.launch {
            notebookRepository.saveNote(
                studyId = study.studyId,
                noteType = NoteType.VERSE,
                title = "Duel : ${duel.title}",
                content = content,
                verseRefLabel = "${duel.passageA.formattedReference()} vs ${duel.passageB.formattedReference()}"
            )
            _uiState.update { it.copy(savedBanner = "Comparaison enregistrée dans le carnet.") }
        }
    }

    fun clearBanner() {
        _uiState.update { it.copy(savedBanner = null) }
    }
}

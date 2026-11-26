package com.harimbola.solascriptura.feature.dailychallenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.DailyChallenge
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.usecase.CalculateDailyChallengeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DailyChallengeUiState(
    val challenge: DailyChallenge? = null,
    val reflectionDraft: String = "",
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DailyChallengeViewModel @Inject constructor(
    studyRepository: StudyRepository,
    private val preferencesRepository: PreferencesRepository,
    private val notebookRepository: NotebookRepository,
    private val calculateDailyChallengeUseCase: CalculateDailyChallengeUseCase
) : ViewModel() {

    private val reflectionDraft = MutableStateFlow("")
    private val messageFlow = MutableStateFlow<String?>(null)

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<DailyChallengeUiState> = combine(
        studiesFlow,
        preferencesRepository.userPreferences,
        reflectionDraft,
        messageFlow
    ) { studies, prefs, draft, msg ->
        val challenge = calculateDailyChallengeUseCase(
            studies = studies,
            completedDateIso = prefs.dailyChallengeCompletedDate,
            locale = prefs.study.language
        )
        DailyChallengeUiState(
            challenge = challenge,
            reflectionDraft = draft,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DailyChallengeUiState()
    )

    fun updateReflection(text: String) {
        reflectionDraft.value = text
    }

    fun markCompleted() {
        val challenge = uiState.value.challenge ?: return
        viewModelScope.launch {
            preferencesRepository.setDailyChallengeCompletedDate(challenge.dateIso)
            val note = reflectionDraft.value.trim()
            if (note.isNotEmpty()) {
                notebookRepository.saveNote(
                    studyId = challenge.studyId,
                    noteType = NoteType.NOTE,
                    title = "Défi du ${challenge.dateIso}",
                    content = note,
                    verseRefLabel = challenge.verses.joinToString(", ") { it.formattedReference() }
                )
            }
            messageFlow.value = "Défi quotidien enregistré hors ligne."
        }
    }

    fun clearMessage() {
        messageFlow.value = null
    }
}

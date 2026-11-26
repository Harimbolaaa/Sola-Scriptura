package com.harimbola.solascriptura.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.StudyProgressStats
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.usecase.CalculateProgressStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class StudyProgressItem(
    val study: Study,
    val progress: StudyProgress
)

data class ProgressUiState(
    val stats: StudyProgressStats = StudyProgressStats(),
    val items: List<StudyProgressItem> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProgressViewModel @Inject constructor(
    studyRepository: StudyRepository,
    progressRepository: ProgressRepository,
    notebookRepository: NotebookRepository,
    preferencesRepository: PreferencesRepository,
    private val calculateProgressStatsUseCase: CalculateProgressStatsUseCase
) : ViewModel() {

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<ProgressUiState> = combine(
        studiesFlow,
        progressRepository.observeAllProgress(),
        notebookRepository.observeAllNotes(),
        progressRepository.observeVisitedNodesCount()
    ) { studies, progressList, notes, visitedCount ->
        val studyMap = studies.associateBy { it.studyId }
        val stats = calculateProgressStatsUseCase(studies, progressList, notes, visitedCount)
        val items = progressList.mapNotNull { prog ->
            studyMap[prog.studyId]?.let { StudyProgressItem(it, prog) }
        }
        ProgressUiState(stats = stats, items = items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProgressUiState()
    )
}

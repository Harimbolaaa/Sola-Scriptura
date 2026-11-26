package com.harimbola.solascriptura.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.Category
import com.harimbola.solascriptura.domain.model.DailyChallenge
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.StudyProgressStats
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.usecase.CalculateDailyChallengeUseCase
import com.harimbola.solascriptura.domain.usecase.CalculateProgressStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val resumeStudy: Study? = null,
    val resumeProgress: StudyProgress? = null,
    val dailyChallenge: DailyChallenge? = null,
    val stats: StudyProgressStats = StudyProgressStats(),
    val recommendedStudies: List<Study> = emptyList(),
    val downloadedStudies: List<Study> = emptyList(),
    val categories: List<Category> = emptyList(),
    val notesCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    progressRepository: ProgressRepository,
    notebookRepository: NotebookRepository,
    preferencesRepository: PreferencesRepository,
    private val calculateDailyChallengeUseCase: CalculateDailyChallengeUseCase,
    private val calculateProgressStatsUseCase: CalculateProgressStatsUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            studyRepository.ensureBundledStudiesImported()
        }
    }

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        studiesFlow,
        studyRepository.observeCategories(),
        progressRepository.observeAllProgress(),
        notebookRepository.observeAllNotes(),
        preferencesRepository.userPreferences,
        progressRepository.observeVisitedNodesCount()
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val studies = values[0] as List<Study>
        @Suppress("UNCHECKED_CAST")
        val categories = values[1] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val progressList = values[2] as List<StudyProgress>
        @Suppress("UNCHECKED_CAST")
        val notes = values[3] as List<com.harimbola.solascriptura.domain.model.UserNote>
        val prefs = values[4] as com.harimbola.solascriptura.core.designsystem.UserPreferences
        val visitedCount = values[5] as Int

        val studyMap = studies.associateBy { it.studyId }
        val inProgress = progressList.firstOrNull { !it.isCompleted }
        val resumeStudy = inProgress?.let { studyMap[it.studyId] }
            ?: progressList.firstOrNull()?.let { studyMap[it.studyId] }

        val completedIds = progressList.filter { it.isCompleted }.map { it.studyId }.toSet()
        val recommended = studies
            .filter { it.studyId !in completedIds }
            .take(4)
            .ifEmpty { studies.take(4) }

        val downloaded = studies.filter { !it.isBundledAsset }

        val challenge = calculateDailyChallengeUseCase(
            studies = studies,
            completedDateIso = prefs.dailyChallengeCompletedDate,
            locale = prefs.study.language
        )

        val stats = calculateProgressStatsUseCase(
            studies = studies,
            progressList = progressList,
            notes = notes,
            visitedNodesCount = visitedCount
        )

        HomeUiState(
            isLoading = false,
            resumeStudy = resumeStudy,
            resumeProgress = inProgress,
            dailyChallenge = challenge,
            stats = stats,
            recommendedStudies = recommended,
            downloadedStudies = downloaded,
            categories = categories,
            notesCount = notes.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )
}

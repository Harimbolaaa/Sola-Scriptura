package com.harimbola.solascriptura.feature.studies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.Category
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
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

data class StudiesUiState(
    val categories: List<Category> = emptyList(),
    val studies: List<Study> = emptyList(),
    val selectedCategoryId: String? = null,
    val searchQuery: String = "",
    val progressByStudyId: Map<String, StudyProgress> = emptyMap(),
    val favoriteIds: Set<String> = emptySet()
)

data class StudyDetailUiState(
    val study: Study? = null,
    val progress: StudyProgress? = null,
    val isFavorite: Boolean = false,
    val relatedStudies: List<Study> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudiesViewModel @Inject constructor(
    private val studyRepository: StudyRepository,
    private val progressRepository: ProgressRepository,
    private val notebookRepository: NotebookRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val selectedCategory = MutableStateFlow<String?>(null)
    private val searchQuery = MutableStateFlow("")

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<StudiesUiState> = combine(
        studyRepository.observeCategories(),
        studiesFlow,
        selectedCategory,
        searchQuery,
        progressRepository.observeAllProgress(),
        notebookRepository.observeFavoriteStudyIds()
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val categories = values[0] as List<Category>
        @Suppress("UNCHECKED_CAST")
        val allStudies = values[1] as List<Study>
        val catFilter = values[2] as String?
        val query = values[3] as String
        @Suppress("UNCHECKED_CAST")
        val progressList = values[4] as List<StudyProgress>
        @Suppress("UNCHECKED_CAST")
        val favs = values[5] as Set<String>

        val filtered = allStudies.filter { study ->
            val catOk = catFilter == null || study.categoryId == catFilter
            val q = query.trim().lowercase()
            val queryOk = q.isEmpty() ||
                study.title.lowercase().contains(q) ||
                study.subtitle.lowercase().contains(q) ||
                study.description.lowercase().contains(q) ||
                study.tags.any { it.lowercase().contains(q) }
            catOk && queryOk
        }

        StudiesUiState(
            categories = categories,
            studies = filtered,
            selectedCategoryId = catFilter,
            searchQuery = query,
            progressByStudyId = progressList.associateBy { it.studyId },
            favoriteIds = favs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudiesUiState()
    )

    fun selectCategory(categoryId: String?) {
        selectedCategory.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun toggleFavorite(studyId: String) {
        viewModelScope.launch {
            notebookRepository.toggleFavorite(studyId)
        }
    }

    fun observeStudyDetail(studyId: String): StateFlow<StudyDetailUiState> = combine(
        studiesFlow,
        progressRepository.observeProgressForStudy(studyId),
        notebookRepository.observeIsFavorite(studyId)
    ) { allStudies, progress, isFav ->
        val study = allStudies.firstOrNull { it.studyId == studyId }
        val relatedIds = study?.graph?.conclusions?.values
            ?.flatMap { it.relatedStudyIds }
            ?.toSet()
            .orEmpty()
        val related = allStudies.filter { it.studyId in relatedIds }
        StudyDetailUiState(
            study = study,
            progress = progress,
            isFavorite = isFav,
            relatedStudies = related
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StudyDetailUiState()
    )

    fun restartStudy(studyId: String) {
        viewModelScope.launch {
            progressRepository.resetStudyProgress(studyId)
        }
    }
}

package com.harimbola.solascriptura.feature.truthmap

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.TruthTreeGraph
import com.harimbola.solascriptura.domain.model.TruthTreeNode
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.usecase.BuildTruthTreeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TruthTreeUiState(
    val graph: TruthTreeGraph = TruthTreeGraph(emptyList(), emptyList()),
    val selectedNode: TruthTreeNode? = null,
    val selectedStudy: Study? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TruthTreeViewModel @Inject constructor(
    studyRepository: StudyRepository,
    progressRepository: ProgressRepository,
    preferencesRepository: PreferencesRepository,
    private val buildTruthTreeUseCase: BuildTruthTreeUseCase
) : ViewModel() {

    private val selectedStudyId = MutableStateFlow<String?>(null)

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<TruthTreeUiState> = combine(
        studiesFlow,
        progressRepository.observeAllProgress(),
        selectedStudyId
    ) { studies, progressList, selectedId ->
        val graph = buildTruthTreeUseCase(studies, progressList)
        val effectiveSelectedId = selectedId ?: graph.nodes.firstOrNull()?.studyId
        TruthTreeUiState(
            graph = graph,
            selectedNode = graph.nodes.firstOrNull { it.studyId == effectiveSelectedId },
            selectedStudy = studies.firstOrNull { it.studyId == effectiveSelectedId }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TruthTreeUiState()
    )

    fun selectNode(studyId: String) {
        selectedStudyId.value = studyId
    }
}

package com.harimbola.solascriptura.feature.notebook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.UserNote
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
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

enum class NotebookFilter {
    ALL,
    CONCLUSION,
    QUESTION,
    NOTE,
    VERSE,
    DIFFICULTY,
    FAVORITES
}

data class NotebookUiState(
    val notes: List<UserNote> = emptyList(),
    val favoriteStudies: List<Study> = emptyList(),
    val studiesMap: Map<String, Study> = emptyMap(),
    val filter: NotebookFilter = NotebookFilter.ALL,
    val exportedMarkdown: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotebookViewModel @Inject constructor(
    private val notebookRepository: NotebookRepository,
    studyRepository: StudyRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val filterFlow = MutableStateFlow(NotebookFilter.ALL)
    private val exportedFlow = MutableStateFlow<String?>(null)

    private val studiesFlow = preferencesRepository.userPreferences.flatMapLatest { prefs ->
        studyRepository.observeStudies(prefs.study.language)
    }

    val uiState: StateFlow<NotebookUiState> = combine(
        notebookRepository.observeAllNotes(),
        notebookRepository.observeFavoriteStudyIds(),
        studiesFlow,
        filterFlow,
        exportedFlow
    ) { notes, favIds, studies, filter, exported ->
        val studyMap = studies.associateBy { it.studyId }
        val filteredNotes = when (filter) {
            NotebookFilter.ALL -> notes
            NotebookFilter.CONCLUSION -> notes.filter { it.noteType == NoteType.CONCLUSION }
            NotebookFilter.QUESTION -> notes.filter { it.noteType == NoteType.QUESTION }
            NotebookFilter.NOTE -> notes.filter { it.noteType == NoteType.NOTE }
            NotebookFilter.VERSE -> notes.filter { it.noteType == NoteType.VERSE }
            NotebookFilter.DIFFICULTY -> notes.filter { it.noteType == NoteType.DIFFICULTY }
            NotebookFilter.FAVORITES -> emptyList()
        }
        val favStudies = studies.filter { it.studyId in favIds }
        NotebookUiState(
            notes = filteredNotes,
            favoriteStudies = favStudies,
            studiesMap = studyMap,
            filter = filter,
            exportedMarkdown = exported
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotebookUiState()
    )

    fun setFilter(filter: NotebookFilter) {
        filterFlow.value = filter
    }

    fun saveNote(
        id: String? = null,
        studyId: String,
        noteType: NoteType,
        title: String,
        content: String,
        verseRefLabel: String?
    ) {
        if (content.isBlank()) return
        viewModelScope.launch {
            notebookRepository.saveNote(
                id = id,
                studyId = studyId,
                nodeId = null,
                noteType = noteType,
                title = title,
                content = content,
                verseRefLabel = verseRefLabel
            )
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            notebookRepository.deleteNote(noteId)
        }
    }

    fun toggleFavorite(studyId: String) {
        viewModelScope.launch {
            notebookRepository.toggleFavorite(studyId)
        }
    }

    fun prepareExport() {
        viewModelScope.launch {
            val md = notebookRepository.exportNotesAsMarkdown(uiState.value.studiesMap)
            exportedFlow.value = md
        }
    }

    fun consumeExport() {
        exportedFlow.value = null
    }
}

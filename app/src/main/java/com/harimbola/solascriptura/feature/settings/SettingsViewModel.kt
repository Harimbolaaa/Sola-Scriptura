package com.harimbola.solascriptura.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.core.designsystem.AccentPreset
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.CardDensity
import com.harimbola.solascriptura.core.designsystem.FontStylePreset
import com.harimbola.solascriptura.core.designsystem.SyncMode
import com.harimbola.solascriptura.core.designsystem.ThemeMode
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val preferences: UserPreferences = UserPreferences(),
    val installedStudiesCount: Int = 0,
    val totalStorageBytes: Long = 0L,
    val pendingSyncCount: Int = 0,
    val message: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val studyRepository: StudyRepository,
    private val progressRepository: ProgressRepository,
    private val notebookRepository: NotebookRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val messageFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.userPreferences,
        studyRepository.observeStudies(),
        studyRepository.observeTotalStorageBytes(),
        syncRepository.observePendingQueueCount(),
        messageFlow
    ) { prefs, studies, bytes, pending, msg ->
        SettingsUiState(
            preferences = prefs,
            installedStudiesCount = studies.size,
            totalStorageBytes = bytes,
            pendingSyncCount = pending,
            message = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun setVisualTheme(theme: AppVisualTheme) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(visualTheme = theme) }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(themeMode = mode) }
        }
    }

    fun setAccentPreset(preset: AccentPreset) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(accentPreset = preset) }
        }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(useDynamicColors = enabled) }
        }
    }

    fun setFontStyle(preset: FontStylePreset) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(fontStyle = preset) }
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(fontScale = scale) }
        }
    }

    fun setLineSpacing(multiplier: Float) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(lineSpacingMultiplier = multiplier) }
        }
    }

    fun setCardDensity(density: CardDensity) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(cardDensity = density) }
        }
    }

    fun setAnimationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(animationsEnabled = enabled) }
        }
    }

    fun setLanguage(locale: String) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(language = locale) }
        }
    }

    fun setPreferredDuration(minutes: Int) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(preferredDurationMinutes = minutes) }
        }
    }

    fun setAutoResume(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(autoResumeLastStudy = enabled) }
        }
    }

    fun setShowVerseReferences(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(showVerseReferences = show) }
        }
    }

    fun setShowBiblicalContext(show: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(showBiblicalContext = show) }
        }
    }

    fun setSyncMode(mode: SyncMode) {
        viewModelScope.launch {
            preferencesRepository.updateSyncPreferences { it.copy(syncMode = mode) }
        }
    }

    fun setSyncSettingsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSyncPreferences { it.copy(syncSettingsEnabled = enabled) }
        }
    }

    fun clearDownloadedCache() {
        viewModelScope.launch {
            studyRepository.clearDownloadedCache()
            messageFlow.value = "Cache nettoyé. Vos progressions et notes ont été conservées."
        }
    }

    fun clearAllUserProgress() {
        viewModelScope.launch {
            progressRepository.clearAllLocalProgress()
            notebookRepository.clearAllNotes()
            messageFlow.value = "Progressions et notes locales supprimées."
        }
    }

    fun clearMessage() {
        messageFlow.value = null
    }
}

package com.harimbola.solascriptura.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.ThemeMode
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val studyRepository: StudyRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            studyRepository.ensureBundledStudiesImported()
        }
    }

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun selectTheme(theme: AppVisualTheme) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(visualTheme = theme) }
        }
    }

    fun selectThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemePreferences { it.copy(themeMode = mode) }
        }
    }

    fun selectLanguage(locale: String) {
        viewModelScope.launch {
            preferencesRepository.updateStudyPreferences { it.copy(language = locale) }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted(true)
        }
    }
}

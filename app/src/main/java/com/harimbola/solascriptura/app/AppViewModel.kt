package com.harimbola.solascriptura.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.core.designsystem.SyncMode
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    preferencesRepository: PreferencesRepository,
    private val studyRepository: StudyRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences()
        )

    init {
        viewModelScope.launch {
            studyRepository.ensureBundledStudiesImported()
            val prefs = preferencesRepository.userPreferences.first()
            if (prefs.sync.syncMode != SyncMode.MANUAL) {
                syncRepository.schedulePeriodicSync()
            }
        }
    }
}

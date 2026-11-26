package com.harimbola.solascriptura.feature.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.domain.model.AppUser
import com.harimbola.solascriptura.domain.model.RemoteCatalogItem
import com.harimbola.solascriptura.domain.repository.AuthRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isAppwriteConfigured: Boolean = false,
    val user: AppUser? = null,
    val isBusy: Boolean = false,
    val pendingQueueCount: Int = 0,
    val lastSyncTimestamp: Long = 0L,
    val remoteCatalog: List<RemoteCatalogItem> = emptyList(),
    val statusMessage: String? = null,
    val error: AppError? = null
)

private data class AccountTransientState(
    val isBusy: Boolean = false,
    val remoteCatalog: List<RemoteCatalogItem> = emptyList(),
    val statusMessage: String? = null,
    val error: AppError? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val transientState = MutableStateFlow(AccountTransientState())

    val uiState: StateFlow<AccountUiState> = combine(
        authRepository.observeCurrentUser(),
        syncRepository.observePendingQueueCount(),
        preferencesRepository.userPreferences,
        transientState
    ) { user, pendingCount, prefs, transient ->
        AccountUiState(
            isAppwriteConfigured = authRepository.isAppwriteConfigured,
            user = user,
            isBusy = transient.isBusy,
            pendingQueueCount = pendingCount,
            lastSyncTimestamp = prefs.sync.lastSyncTimestamp,
            remoteCatalog = transient.remoteCatalog,
            statusMessage = transient.statusMessage,
            error = transient.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AccountUiState(isAppwriteConfigured = authRepository.isAppwriteConfigured)
    )

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = authRepository.signIn(email, password)) {
                is AppResult.Success -> {
                    transientState.update {
                        it.copy(
                            isBusy = false,
                            statusMessage = "Connecté en tant que ${res.data.name}. Vous pouvez transférer votre progression locale."
                        )
                    }
                }
                is AppResult.Failure -> {
                    transientState.update { it.copy(isBusy = false, error = res.error) }
                }
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = authRepository.signUp(name, email, password)) {
                is AppResult.Success -> {
                    transientState.update {
                        it.copy(
                            isBusy = false,
                            statusMessage = "Compte créé. Souhaitez-vous transférer votre progression locale vers Appwrite ?"
                        )
                    }
                }
                is AppResult.Failure -> {
                    transientState.update { it.copy(isBusy = false, error = res.error) }
                }
            }
        }
    }

    fun sendRecovery(email: String) {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = authRepository.sendRecoveryEmail(email)) {
                is AppResult.Success -> transientState.update {
                    it.copy(isBusy = false, statusMessage = "E-mail de récupération envoyé.")
                }
                is AppResult.Failure -> transientState.update {
                    it.copy(isBusy = false, error = res.error)
                }
            }
        }
    }

    fun transferLocalProgress() {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = syncRepository.transferLocalProgressToCloud()) {
                is AppResult.Success -> transientState.update {
                    it.copy(isBusy = false, statusMessage = "${res.data} étude(s) transférée(s) vers le cloud.")
                }
                is AppResult.Failure -> transientState.update {
                    it.copy(isBusy = false, error = res.error)
                }
            }
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = syncRepository.syncPendingQueueAndProgress()) {
                is AppResult.Success -> {
                    val catalogRes = syncRepository.fetchRemoteCatalog()
                    val catalog = (catalogRes as? AppResult.Success)?.data ?: emptyList()
                    transientState.update {
                        it.copy(
                            isBusy = false,
                            remoteCatalog = catalog,
                            statusMessage = "Synchronisation terminée (${res.data} opération(s))."
                        )
                    }
                }
                is AppResult.Failure -> {
                    transientState.update { it.copy(isBusy = false, error = res.error) }
                }
            }
        }
    }

    fun downloadCatalogItem(item: RemoteCatalogItem) {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null, statusMessage = null) }
            when (val res = syncRepository.downloadAndInstallStudy(item)) {
                is AppResult.Success -> transientState.update {
                    it.copy(isBusy = false, statusMessage = "Étude « ${res.data.title} » installée hors ligne.")
                }
                is AppResult.Failure -> transientState.update {
                    it.copy(isBusy = false, error = res.error)
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            transientState.update { it.copy(isBusy = true, error = null) }
            authRepository.signOut()
            transientState.update {
                it.copy(isBusy = false, statusMessage = "Déconnecté. Vos données locales sont conservées.")
            }
        }
    }
}

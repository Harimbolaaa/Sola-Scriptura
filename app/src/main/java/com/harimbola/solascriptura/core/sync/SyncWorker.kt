package com.harimbola.solascriptura.core.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.core.designsystem.SyncMode
import com.harimbola.solascriptura.core.network.NetworkMonitor
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val preferencesRepository: PreferencesRepository,
    private val networkMonitor: NetworkMonitor
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = preferencesRepository.userPreferences.first()
        if (!networkMonitor.isOnline()) {
            return Result.retry()
        }
        if (prefs.sync.syncMode == SyncMode.WIFI_ONLY && !networkMonitor.isUnmeteredWifi()) {
            return Result.retry()
        }

        // 1. Sync pending user progress
        val progressSync = syncRepository.syncPendingQueueAndProgress()
        // 2. Refresh catalog and install updated studies atomically if available
        val catalogResult = syncRepository.fetchRemoteCatalog()
        if (catalogResult is AppResult.Success) {
            catalogResult.data.take(10).forEach { item ->
                syncRepository.downloadAndInstallStudy(item)
            }
        }

        return if (progressSync is AppResult.Failure && runAttemptCount < 3) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}

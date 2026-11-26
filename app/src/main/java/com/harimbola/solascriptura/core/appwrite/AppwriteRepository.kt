package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.domain.model.AppUser
import com.harimbola.solascriptura.domain.model.RemoteCatalogItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteRepository @Inject constructor(
    private val clientProvider: AppwriteClientProvider,
    private val accountDataSource: AppwriteAccountDataSource,
    private val studyDataSource: AppwriteStudyDataSource,
    private val progressDataSource: AppwriteProgressDataSource,
    private val storageDataSource: AppwriteStorageDataSource
) {
    val isConfigured: Boolean
        get() = clientProvider.isConfigured()

    suspend fun getCurrentUser(): AppResult<AppUser?> = accountDataSource.getCurrentUser()
    suspend fun signIn(email: String, password: String): AppResult<AppUser> = accountDataSource.signIn(email, password)
    suspend fun signUp(name: String, email: String, password: String): AppResult<AppUser> =
        accountDataSource.createAccount(name, email, password)
    suspend fun sendRecovery(email: String): AppResult<Unit> = accountDataSource.sendRecovery(email)
    suspend fun signOut(): AppResult<Unit> = accountDataSource.signOut()

    suspend fun fetchCatalog(): AppResult<List<RemoteCatalogItem>> = studyDataSource.fetchPublishedCatalog()
    suspend fun downloadStudyJson(fileId: String): AppResult<String> = storageDataSource.downloadStudyJson(fileId)
    suspend fun listProgress(userId: String): AppResult<List<RemoteProgressPayload>> =
        progressDataSource.listUserProgress(userId)
    suspend fun upsertProgress(payload: RemoteProgressPayload): AppResult<Unit> =
        progressDataSource.upsertUserProgress(payload)
}

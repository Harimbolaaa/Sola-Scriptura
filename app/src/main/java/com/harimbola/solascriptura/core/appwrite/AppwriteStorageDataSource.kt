package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppConstants
import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteStorageDataSource @Inject constructor(
    private val clientProvider: AppwriteClientProvider
) {
    suspend fun downloadStudyJson(fileId: String): AppResult<String> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        val sanitizedFileId = fileId.trim()
        if (sanitizedFileId.isBlank() || sanitizedFileId.contains("..") || sanitizedFileId.contains('/')) {
            return AppResult.Failure(AppError.ResourceNotFound)
        }
        return try {
            val bytes = clientProvider.storage.getFileDownload(
                bucketId = clientProvider.studyBucketId,
                fileId = sanitizedFileId
            )
            if (bytes.size > AppConstants.MAX_JSON_SIZE_BYTES) {
                return AppResult.Failure(AppError.CorruptedJson("File size exceeds safety limit."))
            }
            AppResult.Success(bytes.toString(Charsets.UTF_8))
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }
}

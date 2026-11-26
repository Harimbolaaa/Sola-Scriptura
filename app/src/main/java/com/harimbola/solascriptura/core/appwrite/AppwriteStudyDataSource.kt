package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.domain.model.RemoteCatalogItem
import io.appwrite.Query
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppwriteStudyDataSource @Inject constructor(
    private val clientProvider: AppwriteClientProvider
) {
    suspend fun fetchPublishedCatalog(): AppResult<List<RemoteCatalogItem>> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            val rowList = clientProvider.tablesDB.listRows(
                databaseId = clientProvider.databaseId,
                tableId = clientProvider.catalogTableId,
                queries = listOf(
                    Query.equal("published", true),
                    Query.orderAsc("sortOrder"),
                    Query.limit(100)
                )
            )
            val items = rowList.rows.mapNotNull { row ->
                val data = row.data
                val studyId = data["studyId"] as? String ?: return@mapNotNull null
                val storageFileId = data["storageFileId"] as? String ?: return@mapNotNull null
                RemoteCatalogItem(
                    studyId = studyId,
                    title = data["title"] as? String ?: studyId,
                    subtitle = data["subtitle"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    categoryId = data["categoryId"] as? String ?: "foundations",
                    locale = data["locale"] as? String ?: "fr",
                    contentVersion = (data["contentVersion"] as? Number)?.toInt() ?: 1,
                    schemaVersion = (data["schemaVersion"] as? Number)?.toInt() ?: 1,
                    storageFileId = storageFileId,
                    checksum = data["checksum"] as? String ?: "",
                    estimatedMinutes = (data["estimatedMinutes"] as? Number)?.toInt() ?: 10,
                    difficulty = data["difficulty"] as? String ?: "BEGINNER",
                    editorialStatus = data["editorialStatus"] as? String ?: "REVIEWED",
                    published = data["published"] as? Boolean ?: true
                )
            }
            AppResult.Success(items)
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }
}

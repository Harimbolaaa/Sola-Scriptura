package com.harimbola.solascriptura.core.appwrite

import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import io.appwrite.Permission
import io.appwrite.Query
import io.appwrite.Role
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class RemoteProgressPayload(
    val userId: String,
    val studyId: String,
    val currentNodeId: String,
    val progressJson: String,
    val completed: Boolean,
    val startedAt: Long,
    val completedAt: Long?,
    val updatedAt: Long
)

@Singleton
class AppwriteProgressDataSource @Inject constructor(
    private val clientProvider: AppwriteClientProvider
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun listUserProgress(userId: String): AppResult<List<RemoteProgressPayload>> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        return try {
            val response = clientProvider.tablesDB.listRows(
                databaseId = clientProvider.databaseId,
                tableId = clientProvider.progressTableId,
                queries = listOf(
                    Query.equal("userId", userId),
                    Query.limit(200)
                )
            )
            val result = response.rows.mapNotNull { row ->
                val d = row.data
                val studyId = d["studyId"] as? String ?: return@mapNotNull null
                RemoteProgressPayload(
                    userId = d["userId"] as? String ?: userId,
                    studyId = studyId,
                    currentNodeId = d["currentNodeId"] as? String ?: "",
                    progressJson = d["progressJson"] as? String ?: "{}",
                    completed = d["completed"] as? Boolean ?: false,
                    startedAt = (d["startedAt"] as? Number)?.toLong() ?: 0L,
                    completedAt = (d["completedAt"] as? Number)?.toLong(),
                    updatedAt = (d["updatedAt"] as? Number)?.toLong() ?: 0L
                )
            }
            AppResult.Success(result)
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }

    suspend fun upsertUserProgress(payload: RemoteProgressPayload): AppResult<Unit> {
        if (!clientProvider.isConfigured()) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        val rowId = buildSafeRowId(payload.userId, payload.studyId)
        val userPerms = listOf(
            Permission.read(Role.user(payload.userId)),
            Permission.write(Role.user(payload.userId))
        )
        val data = mapOf(
            "userId" to payload.userId,
            "studyId" to payload.studyId,
            "currentNodeId" to payload.currentNodeId,
            "progressJson" to payload.progressJson,
            "completed" to payload.completed,
            "startedAt" to payload.startedAt,
            "completedAt" to payload.completedAt,
            "updatedAt" to payload.updatedAt
        )
        return try {
            clientProvider.tablesDB.upsertRow(
                databaseId = clientProvider.databaseId,
                tableId = clientProvider.progressTableId,
                rowId = rowId,
                data = data,
                permissions = userPerms
            )
            AppResult.Success(Unit)
        } catch (t: Throwable) {
            AppResult.Failure(AppwriteErrorMapper.map(t))
        }
    }

    private fun buildSafeRowId(userId: String, studyId: String): String {
        val raw = "${userId.take(12)}_${studyId.take(20)}"
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
        return raw.take(36).trimStart('_', '-', '.')
            .ifBlank { "row_${studyId.take(24)}" }
    }
}

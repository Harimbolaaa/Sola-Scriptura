package com.harimbola.solascriptura.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "studies",
    indices = [
        Index(value = ["studyId"]),
        Index(value = ["categoryId"]),
        Index(value = ["locale"]),
        Index(value = ["isAvailable"])
    ]
)
data class StudyEntity(
    @PrimaryKey val id: String,
    val studyId: String,
    val schemaVersion: Int,
    val contentVersion: Int,
    val locale: String,
    val title: String,
    val subtitle: String,
    val categoryId: String,
    val description: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val tagsJson: String,
    val perspective: String,
    val jsonContent: String,
    val checksum: String,
    val editorialStatus: String,
    val downloadedAt: Long,
    val lastUpdatedAt: Long,
    val isAvailable: Boolean,
    val isBundledAsset: Boolean = true
)

@Entity(
    tableName = "dialogue_progress",
    indices = [
        Index(value = ["studyId"], unique = true),
        Index(value = ["lastOpenedAt"])
    ]
)
data class DialogueProgressEntity(
    @PrimaryKey val id: String,
    val studyId: String,
    val currentNodeId: String,
    val historyJson: String,
    val answersJson: String,
    val unlockedNotionsJson: String = "[]",
    val personalConclusion: String = "",
    val isUnconvinced: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAt: Long,
    val lastOpenedAt: Long,
    val completedAt: Long? = null,
    val updatedAt: Long
)

@Entity(
    tableName = "user_notes",
    indices = [
        Index(value = ["studyId"]),
        Index(value = ["noteType"]),
        Index(value = ["updatedAt"])
    ]
)
data class UserNoteEntity(
    @PrimaryKey val id: String,
    val studyId: String,
    val nodeId: String?,
    val noteType: String, // CONCLUSION, QUESTION, NOTE, VERSE, DIFFICULTY
    val title: String,
    val content: String,
    val verseRefLabel: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "favorites",
    indices = [Index(value = ["studyId"], unique = true)]
)
data class FavoriteEntity(
    @PrimaryKey val id: String,
    val studyId: String,
    val createdAt: Long
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["sortOrder"])]
)
data class CategoryEntity(
    @PrimaryKey val categoryId: String,
    val title: String,
    val description: String,
    val sortOrder: Int,
    val icon: String
)

@Entity(
    tableName = "sync_queue",
    indices = [
        Index(value = ["status"]),
        Index(value = ["entityId"]),
        Index(value = ["createdAt"])
    ]
)
data class SyncQueueEntity(
    @PrimaryKey val operationId: String,
    val operationType: String, // UPSERT_PROGRESS, UPSERT_NOTE, DELETE_NOTE, UPSERT_FAVORITE, DELETE_FAVORITE
    val entityId: String,
    val payload: String,
    val createdAt: Long,
    val retryCount: Int = 0,
    val status: String = "PENDING" // PENDING, IN_PROGRESS, FAILED, DONE
)

@Entity(
    tableName = "reading_history",
    indices = [
        Index(value = ["studyId"]),
        Index(value = ["visitedAt"])
    ]
)
data class ReadingHistoryEntity(
    @PrimaryKey val id: String,
    val studyId: String,
    val nodeId: String,
    val visitedAt: Long
)

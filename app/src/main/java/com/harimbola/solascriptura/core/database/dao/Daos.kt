package com.harimbola.solascriptura.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.harimbola.solascriptura.core.database.entity.CategoryEntity
import com.harimbola.solascriptura.core.database.entity.DialogueProgressEntity
import com.harimbola.solascriptura.core.database.entity.FavoriteEntity
import com.harimbola.solascriptura.core.database.entity.ReadingHistoryEntity
import com.harimbola.solascriptura.core.database.entity.StudyEntity
import com.harimbola.solascriptura.core.database.entity.SyncQueueEntity
import com.harimbola.solascriptura.core.database.entity.UserNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {
    @Query("SELECT * FROM studies WHERE isAvailable = 1 ORDER BY categoryId, title")
    fun observeAvailableStudies(): Flow<List<StudyEntity>>

    @Query("SELECT * FROM studies WHERE isAvailable = 1 AND categoryId = :categoryId ORDER BY title")
    fun observeStudiesByCategory(categoryId: String): Flow<List<StudyEntity>>

    @Query("SELECT * FROM studies WHERE studyId = :studyId AND locale = :locale LIMIT 1")
    suspend fun getStudyByIdAndLocale(studyId: String, locale: String): StudyEntity?

    @Query("SELECT * FROM studies WHERE studyId = :studyId")
    suspend fun getStudiesById(studyId: String): List<StudyEntity>

    @Query("SELECT * FROM studies WHERE studyId = :studyId")
    fun observeStudiesById(studyId: String): Flow<List<StudyEntity>>

    @Query("SELECT * FROM studies WHERE isAvailable = 1")
    suspend fun getAllAvailableStudies(): List<StudyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudy(study: StudyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudies(studies: List<StudyEntity>)

    @Transaction
    suspend fun atomicReplaceStudy(study: StudyEntity) {
        upsertStudy(study)
    }

    @Query("DELETE FROM studies WHERE isBundledAsset = 0 AND id = :id")
    suspend fun deleteDownloadedStudy(id: String)

    @Query("SELECT COALESCE(SUM(LENGTH(jsonContent)), 0) FROM studies")
    fun observeTotalStorageBytes(): Flow<Long>
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM dialogue_progress ORDER BY lastOpenedAt DESC")
    fun observeAllProgress(): Flow<List<DialogueProgressEntity>>

    @Query("SELECT * FROM dialogue_progress WHERE studyId = :studyId LIMIT 1")
    fun observeProgressForStudy(studyId: String): Flow<DialogueProgressEntity?>

    @Query("SELECT * FROM dialogue_progress WHERE studyId = :studyId LIMIT 1")
    suspend fun getProgressForStudy(studyId: String): DialogueProgressEntity?

    @Query("SELECT * FROM dialogue_progress WHERE isCompleted = 0 ORDER BY lastOpenedAt DESC LIMIT 1")
    fun observeMostRecentInProgress(): Flow<DialogueProgressEntity?>

    @Query("SELECT * FROM dialogue_progress")
    suspend fun getAllProgressSnapshot(): List<DialogueProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: DialogueProgressEntity)

    @Query("DELETE FROM dialogue_progress WHERE studyId = :studyId")
    suspend fun deleteProgressForStudy(studyId: String)

    @Query("DELETE FROM dialogue_progress")
    suspend fun clearAllProgress()
}

@Dao
interface UserNoteDao {
    @Query("SELECT * FROM user_notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE studyId = :studyId ORDER BY updatedAt DESC")
    fun observeNotesForStudy(studyId: String): Flow<List<UserNoteEntity>>

    @Query("SELECT * FROM user_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: String): UserNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: UserNoteEntity)

    @Query("DELETE FROM user_notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)

    @Query("DELETE FROM user_notes")
    suspend fun clearAllNotes()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY createdAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE studyId = :studyId)")
    fun observeIsFavorite(studyId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE studyId = :studyId")
    suspend fun removeFavorite(studyId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
}

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue WHERE status IN ('PENDING', 'FAILED') ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncQueueEntity>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status IN ('PENDING', 'FAILED')")
    fun observePendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(operation: SyncQueueEntity)

    @Query("UPDATE sync_queue SET status = :status, retryCount = :retryCount WHERE operationId = :operationId")
    suspend fun updateStatus(operationId: String, status: String, retryCount: Int)

    @Query("DELETE FROM sync_queue WHERE status = 'DONE'")
    suspend fun purgeCompleted()

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY visitedAt DESC LIMIT 100")
    fun observeRecentHistory(): Flow<List<ReadingHistoryEntity>>

    @Query("SELECT COUNT(DISTINCT nodeId) FROM reading_history")
    fun observeVisitedNodesCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVisit(entry: ReadingHistoryEntity)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()
}

package com.harimbola.solascriptura.domain.repository

import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.core.designsystem.StudyPreferences
import com.harimbola.solascriptura.core.designsystem.SyncPreferences
import com.harimbola.solascriptura.core.designsystem.ThemePreferences
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import com.harimbola.solascriptura.domain.model.AppUser
import com.harimbola.solascriptura.domain.model.Category
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.RemoteCatalogItem
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.UserNote
import kotlinx.coroutines.flow.Flow

interface StudyRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeStudies(locale: String? = null): Flow<List<Study>>
    fun observeStudyById(studyId: String, locale: String? = null): Flow<Study?>
    suspend fun getStudyById(studyId: String, locale: String? = null): Study?
    suspend fun ensureBundledStudiesImported(): AppResult<Int>
    suspend fun importValidatedStudyJson(rawJson: String, isBundledAsset: Boolean = false): AppResult<Study>
    fun observeTotalStorageBytes(): Flow<Long>
    suspend fun clearDownloadedCache(): AppResult<Unit>
}

interface ProgressRepository {
    fun observeAllProgress(): Flow<List<StudyProgress>>
    fun observeProgressForStudy(studyId: String): Flow<StudyProgress?>
    fun observeMostRecentInProgress(): Flow<StudyProgress?>
    fun observeVisitedNodesCount(): Flow<Int>
    suspend fun saveProgress(progress: StudyProgress, enqueueSync: Boolean = true)
    suspend fun recordNodeVisit(studyId: String, nodeId: String)
    suspend fun resetStudyProgress(studyId: String)
    suspend fun clearAllLocalProgress()
}

interface NotebookRepository {
    fun observeAllNotes(): Flow<List<UserNote>>
    fun observeNotesForStudy(studyId: String): Flow<List<UserNote>>
    fun observeFavoriteStudyIds(): Flow<Set<String>>
    fun observeIsFavorite(studyId: String): Flow<Boolean>
    suspend fun saveNote(
        id: String? = null,
        studyId: String,
        nodeId: String? = null,
        noteType: NoteType,
        title: String,
        content: String,
        verseRefLabel: String? = null
    ): UserNote
    suspend fun deleteNote(noteId: String)
    suspend fun toggleFavorite(studyId: String)
    suspend fun exportNotesAsMarkdown(studiesMap: Map<String, Study>): String
    suspend fun clearAllNotes()
}

interface PreferencesRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun updateThemePreferences(transform: (ThemePreferences) -> ThemePreferences)
    suspend fun updateStudyPreferences(transform: (StudyPreferences) -> StudyPreferences)
    suspend fun updateSyncPreferences(transform: (SyncPreferences) -> SyncPreferences)
    suspend fun setDailyChallengeCompletedDate(dateIso: String)
}

interface AuthRepository {
    val isAppwriteConfigured: Boolean
    fun observeCurrentUser(): Flow<AppUser?>
    suspend fun refreshSession(): AppResult<AppUser?>
    suspend fun signIn(email: String, password: String): AppResult<AppUser>
    suspend fun signUp(name: String, email: String, password: String): AppResult<AppUser>
    suspend fun sendRecoveryEmail(email: String): AppResult<Unit>
    suspend fun signOut(): AppResult<Unit>
}

interface SyncRepository {
    fun observePendingQueueCount(): Flow<Int>
    suspend fun fetchRemoteCatalog(): AppResult<List<RemoteCatalogItem>>
    suspend fun downloadAndInstallStudy(catalogItem: RemoteCatalogItem): AppResult<Study>
    suspend fun syncPendingQueueAndProgress(): AppResult<Int>
    suspend fun transferLocalProgressToCloud(): AppResult<Int>
    fun scheduleSyncNow()
    fun schedulePeriodicSync()
}

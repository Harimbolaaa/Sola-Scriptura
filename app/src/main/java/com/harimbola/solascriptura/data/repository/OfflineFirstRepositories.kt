package com.harimbola.solascriptura.data.repository

import com.harimbola.solascriptura.core.appwrite.AppwriteRepository
import com.harimbola.solascriptura.core.appwrite.RemoteProgressPayload
import com.harimbola.solascriptura.core.common.AppError
import com.harimbola.solascriptura.core.common.AppResult
import com.harimbola.solascriptura.core.database.dao.CategoryDao
import com.harimbola.solascriptura.core.database.dao.FavoriteDao
import com.harimbola.solascriptura.core.database.dao.ProgressDao
import com.harimbola.solascriptura.core.database.dao.ReadingHistoryDao
import com.harimbola.solascriptura.core.database.dao.StudyDao
import com.harimbola.solascriptura.core.database.dao.SyncQueueDao
import com.harimbola.solascriptura.core.database.dao.UserNoteDao
import com.harimbola.solascriptura.core.database.entity.CategoryEntity
import com.harimbola.solascriptura.core.database.entity.FavoriteEntity
import com.harimbola.solascriptura.core.database.entity.ReadingHistoryEntity
import com.harimbola.solascriptura.core.database.entity.SyncQueueEntity
import com.harimbola.solascriptura.core.datastore.PreferencesDataStore
import com.harimbola.solascriptura.core.designsystem.StudyPreferences
import com.harimbola.solascriptura.core.designsystem.SyncPreferences
import com.harimbola.solascriptura.core.designsystem.ThemePreferences
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.json.AssetStudyLoader
import com.harimbola.solascriptura.core.json.DialogueValidator
import com.harimbola.solascriptura.core.json.ValidationResult
import com.harimbola.solascriptura.core.sync.ProgressMerger
import com.harimbola.solascriptura.core.sync.SyncScheduler
import com.harimbola.solascriptura.data.mapper.toDomain
import com.harimbola.solascriptura.data.mapper.toEntity
import com.harimbola.solascriptura.domain.model.AppUser
import com.harimbola.solascriptura.domain.model.Category
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.RemoteCatalogItem
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.UserNote
import com.harimbola.solascriptura.domain.repository.AuthRepository
import com.harimbola.solascriptura.domain.repository.NotebookRepository
import com.harimbola.solascriptura.domain.repository.PreferencesRepository
import com.harimbola.solascriptura.domain.repository.ProgressRepository
import com.harimbola.solascriptura.domain.repository.StudyRepository
import com.harimbola.solascriptura.domain.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyRepositoryImpl @Inject constructor(
    private val studyDao: StudyDao,
    private val categoryDao: CategoryDao,
    private val assetStudyLoader: AssetStudyLoader
) : StudyRepository {

    override fun observeCategories(): Flow<List<Category>> =
        categoryDao.observeCategories().map { list -> list.map { it.toDomain() } }

    override fun observeStudies(locale: String?): Flow<List<Study>> =
        studyDao.observeAvailableStudies().map { entities ->
            val domainStudies = entities.mapNotNull { it.toDomain() }
            if (locale.isNullOrBlank()) {
                domainStudies
            } else {
                // Group by base logical study (e.g. etat_des_morts) if multi-locale variants exist,
                // preferring user's selected locale and falling back cleanly to 'fr' or first available.
                domainStudies
                    .groupBy { baseLogicalId(it.studyId) }
                    .map { (_, variants) ->
                        variants.firstOrNull { it.locale == locale }
                            ?: variants.firstOrNull { it.locale == "fr" }
                            ?: variants.first()
                    }
                    .sortedWith(compareBy({ categoryOrder(it.categoryId) }, { it.title }))
            }
        }

    override fun observeStudyById(studyId: String, locale: String?): Flow<Study?> =
        studyDao.observeStudiesById(studyId).map { variants ->
            pickLocaleVariant(variants, locale)
        }

    override suspend fun getStudyById(studyId: String, locale: String?): Study? =
        pickLocaleVariant(studyDao.getStudiesById(studyId), locale)

    private fun pickLocaleVariant(variants: List<com.harimbola.solascriptura.core.database.entity.StudyEntity>, locale: String?): Study? {
        val studies = variants.mapNotNull { it.toDomain() }
        if (studies.isEmpty()) return null
        if (!locale.isNullOrBlank()) {
            studies.firstOrNull { it.locale == locale }?.let { return it }
        }
        return studies.firstOrNull { it.locale == "fr" } ?: studies.first()
    }

    override suspend fun ensureBundledStudiesImported(): AppResult<Int> = withContext(Dispatchers.IO) {
        val defaultCategories = listOf(
            CategoryEntity("foundations", "Saison 1 — Les Fondations", "Autorité des Écritures, Création et origine du mal", 1, "foundation"),
            CategoryEntity("salvation", "Saison 2 — Le Plan du Salut", "Jésus-Christ, grâce, foi et loi de liberté", 2, "salvation"),
            CategoryEntity("forgotten_truths", "Saison 3 — Les Vérités bibliques", "État des morts, Sabbat, sanctuaire et espérance", 3, "truths"),
            CategoryEntity("prophecies", "Saison 4 — Les Prophéties", "Daniel, Apocalypse et symboles prophétiques", 4, "prophecy")
        )
        categoryDao.upsertCategories(defaultCategories)

        val bundled = assetStudyLoader.loadAllValidBundledStudies()
        var importedCount = 0
        val now = System.currentTimeMillis()
        for (item in bundled) {
            val existing = studyDao.getStudyByIdAndLocale(item.document.studyId, item.document.locale)
            if (existing == null || existing.contentVersion < item.document.contentVersion || existing.checksum != item.checksum) {
                studyDao.atomicReplaceStudy(
                    item.document.toEntity(
                        rawJson = item.rawJson,
                        checksum = item.checksum,
                        isBundledAsset = true,
                        nowMillis = now
                    )
                )
                importedCount++
            }
        }
        AppResult.Success(importedCount)
    }

    override suspend fun importValidatedStudyJson(
        rawJson: String,
        isBundledAsset: Boolean
    ): AppResult<Study> = withContext(Dispatchers.IO) {
        when (val result = DialogueValidator.parseAndValidate(rawJson)) {
            is ValidationResult.Invalid -> {
                AppResult.Failure(AppError.CorruptedJson(result.errors.joinToString("; ")))
            }
            is ValidationResult.Valid -> {
                val now = System.currentTimeMillis()
                val entity = result.document.toEntity(
                    rawJson = rawJson,
                    checksum = result.checksum,
                    isBundledAsset = isBundledAsset,
                    nowMillis = now
                )
                studyDao.atomicReplaceStudy(entity)
                val domain = entity.toDomain()
                if (domain != null) AppResult.Success(domain)
                else AppResult.Failure(AppError.CorruptedJson("Failed to map imported study."))
            }
        }
    }

    override fun observeTotalStorageBytes(): Flow<Long> = studyDao.observeTotalStorageBytes()

    override suspend fun clearDownloadedCache(): AppResult<Unit> = withContext(Dispatchers.IO) {
        val all = studyDao.getAllAvailableStudies()
        all.filter { !it.isBundledAsset }.forEach {
            studyDao.deleteDownloadedStudy(it.id)
        }
        AppResult.Success(Unit)
    }

    private fun baseLogicalId(studyId: String): String =
        studyId.removeSuffix("_fr").removeSuffix("_en").removeSuffix("_mg")

    private fun categoryOrder(cat: String): Int = when (cat) {
        "foundations" -> 1
        "salvation" -> 2
        "forgotten_truths" -> 3
        "prophecies" -> 4
        else -> 5
    }
}

@Singleton
class ProgressRepositoryImpl @Inject constructor(
    private val progressDao: ProgressDao,
    private val syncQueueDao: SyncQueueDao,
    private val readingHistoryDao: ReadingHistoryDao,
    private val progressManager: DialogueProgressManager
) : ProgressRepository {

    override fun observeAllProgress(): Flow<List<StudyProgress>> =
        progressDao.observeAllProgress().map { list -> list.map { it.toDomain(progressManager) } }

    override fun observeProgressForStudy(studyId: String): Flow<StudyProgress?> =
        progressDao.observeProgressForStudy(studyId).map { it?.toDomain(progressManager) }

    override fun observeMostRecentInProgress(): Flow<StudyProgress?> =
        progressDao.observeMostRecentInProgress().map { it?.toDomain(progressManager) }

    override fun observeVisitedNodesCount(): Flow<Int> =
        readingHistoryDao.observeVisitedNodesCount()

    override suspend fun saveProgress(progress: StudyProgress, enqueueSync: Boolean) {
        withContext(Dispatchers.IO) {
            progressDao.upsertProgress(progress.toEntity(progressManager))
            if (enqueueSync) {
                val payloadJson = Json.encodeToString(
                    SyncProgressPayload(
                        studyId = progress.studyId,
                        currentNodeId = progress.currentNodeId,
                        historyJson = progress.historyJson,
                        answersJson = progress.answersJson,
                        unlockedNotions = progress.unlockedNotions,
                        personalConclusion = progress.personalConclusion,
                        isUnconvinced = progress.isUnconvinced,
                        isCompleted = progress.isCompleted,
                        startedAt = progress.startedAt,
                        lastOpenedAt = progress.lastOpenedAt,
                        completedAt = progress.completedAt,
                        updatedAt = progress.updatedAt
                    )
                )
                syncQueueDao.enqueue(
                    SyncQueueEntity(
                        operationId = "prog_${progress.studyId}",
                        operationType = "UPSERT_PROGRESS",
                        entityId = progress.studyId,
                        payload = payloadJson,
                        createdAt = System.currentTimeMillis(),
                        retryCount = 0,
                        status = "PENDING"
                    )
                )
            }
        }
    }

    override suspend fun recordNodeVisit(studyId: String, nodeId: String) {
        withContext(Dispatchers.IO) {
            readingHistoryDao.insertVisit(
                ReadingHistoryEntity(
                    id = "${studyId}_$nodeId",
                    studyId = studyId,
                    nodeId = nodeId,
                    visitedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun resetStudyProgress(studyId: String) {
        withContext(Dispatchers.IO) {
            progressDao.deleteProgressForStudy(studyId)
        }
    }

    override suspend fun clearAllLocalProgress() {
        withContext(Dispatchers.IO) {
            progressDao.clearAllProgress()
            readingHistoryDao.clearHistory()
        }
    }
}

@Serializable
data class SyncProgressPayload(
    val studyId: String,
    val currentNodeId: String,
    val historyJson: String,
    val answersJson: String,
    val unlockedNotions: List<String>,
    val personalConclusion: String,
    val isUnconvinced: Boolean,
    val isCompleted: Boolean,
    val startedAt: Long,
    val lastOpenedAt: Long,
    val completedAt: Long?,
    val updatedAt: Long
)

@Singleton
class NotebookRepositoryImpl @Inject constructor(
    private val userNoteDao: UserNoteDao,
    private val favoriteDao: FavoriteDao
) : NotebookRepository {

    override fun observeAllNotes(): Flow<List<UserNote>> =
        userNoteDao.observeAllNotes().map { list -> list.map { it.toDomain() } }

    override fun observeNotesForStudy(studyId: String): Flow<List<UserNote>> =
        userNoteDao.observeNotesForStudy(studyId).map { list -> list.map { it.toDomain() } }

    override fun observeFavoriteStudyIds(): Flow<Set<String>> =
        favoriteDao.observeFavorites().map { favs -> favs.map { it.studyId }.toSet() }

    override fun observeIsFavorite(studyId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(studyId)

    override suspend fun saveNote(
        id: String?,
        studyId: String,
        nodeId: String?,
        noteType: NoteType,
        title: String,
        content: String,
        verseRefLabel: String?
    ): UserNote = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val noteId = id ?: UUID.randomUUID().toString()
        val existing = userNoteDao.getNoteById(noteId)
        val note = UserNote(
            id = noteId,
            studyId = studyId,
            nodeId = nodeId,
            noteType = noteType,
            title = title.trim(),
            content = content.trim(),
            verseRefLabel = verseRefLabel?.takeIf { it.isNotBlank() },
            createdAt = existing?.createdAt ?: now,
            updatedAt = now
        )
        userNoteDao.upsertNote(note.toEntity())
        note
    }

    override suspend fun deleteNote(noteId: String) {
        withContext(Dispatchers.IO) {
            userNoteDao.deleteNoteById(noteId)
        }
    }

    override suspend fun toggleFavorite(studyId: String) {
        withContext(Dispatchers.IO) {
            val isFav = favoriteDao.observeIsFavorite(studyId).first()
            if (isFav) {
                favoriteDao.removeFavorite(studyId)
            } else {
                favoriteDao.addFavorite(
                    FavoriteEntity(
                        id = "fav_$studyId",
                        studyId = studyId,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    override suspend fun exportNotesAsMarkdown(studiesMap: Map<String, Study>): String =
        withContext(Dispatchers.IO) {
            val notes = userNoteDao.observeAllNotes().first()
            if (notes.isEmpty()) return@withContext "# Sola Scriptura — Carnet de réflexion\n\nAucune note enregistrée."
            buildString {
                appendLine("# Sola Scriptura : Maïeutique — Carnet de réflexion")
                appendLine()
                notes.forEach { note ->
                    val studyTitle = studiesMap[note.studyId]?.title ?: note.studyId
                    appendLine("## ${note.title.ifBlank { studyTitle }}")
                    appendLine("- Étude : $studyTitle")
                    appendLine("- Catégorie : ${note.noteType}")
                    if (!note.verseRefLabel.isNullOrBlank()) {
                        appendLine("- Passage : ${note.verseRefLabel}")
                    }
                    appendLine()
                    appendLine(note.content)
                    appendLine()
                    appendLine("---")
                    appendLine()
                }
            }
        }

    override suspend fun clearAllNotes() {
        withContext(Dispatchers.IO) {
            userNoteDao.clearAllNotes()
            favoriteDao.clearAllFavorites()
        }
    }
}

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore
) : PreferencesRepository {
    override val userPreferences: Flow<UserPreferences> = preferencesDataStore.userPreferences

    override suspend fun setOnboardingCompleted(completed: Boolean) =
        preferencesDataStore.setOnboardingCompleted(completed)

    override suspend fun updateThemePreferences(transform: (ThemePreferences) -> ThemePreferences) =
        preferencesDataStore.updateThemePreferences(transform)

    override suspend fun updateStudyPreferences(transform: (StudyPreferences) -> StudyPreferences) =
        preferencesDataStore.updateStudyPreferences(transform)

    override suspend fun updateSyncPreferences(transform: (SyncPreferences) -> SyncPreferences) =
        preferencesDataStore.updateSyncPreferences(transform)

    override suspend fun setDailyChallengeCompletedDate(dateIso: String) =
        preferencesDataStore.setDailyChallengeCompletedDate(dateIso)
}

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val appwriteRepository: AppwriteRepository,
    private val preferencesDataStore: PreferencesDataStore
) : AuthRepository {

    override val isAppwriteConfigured: Boolean
        get() = appwriteRepository.isConfigured

    override fun observeCurrentUser(): Flow<AppUser?> =
        preferencesDataStore.userPreferences.map { prefs ->
            val id = prefs.accountUserId
            val email = prefs.accountEmail
            val name = prefs.accountName
            if (!id.isNullOrBlank() && !email.isNullOrBlank()) {
                AppUser(userId = id, email = email, name = name ?: email)
            } else {
                null
            }
        }

    override suspend fun refreshSession(): AppResult<AppUser?> {
        val result = appwriteRepository.getCurrentUser()
        if (result is AppResult.Success) {
            val user = result.data
            preferencesDataStore.setAccountInfo(user?.userId, user?.email, user?.name)
        }
        return result
    }

    override suspend fun signIn(email: String, password: String): AppResult<AppUser> {
        val result = appwriteRepository.signIn(email.trim(), password)
        if (result is AppResult.Success) {
            val user = result.data
            preferencesDataStore.setAccountInfo(user.userId, user.email, user.name)
        }
        return result
    }

    override suspend fun signUp(name: String, email: String, password: String): AppResult<AppUser> {
        val result = appwriteRepository.signUp(name.trim(), email.trim(), password)
        if (result is AppResult.Success) {
            val user = result.data
            preferencesDataStore.setAccountInfo(user.userId, user.email, user.name)
        }
        return result
    }

    override suspend fun sendRecoveryEmail(email: String): AppResult<Unit> =
        appwriteRepository.sendRecovery(email.trim())

    override suspend fun signOut(): AppResult<Unit> {
        val result = appwriteRepository.signOut()
        preferencesDataStore.setAccountInfo(null, null, null)
        return result
    }
}

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val appwriteRepository: AppwriteRepository,
    private val studyRepository: StudyRepository,
    private val progressDao: ProgressDao,
    private val syncQueueDao: SyncQueueDao,
    private val preferencesDataStore: PreferencesDataStore,
    private val progressManager: DialogueProgressManager,
    private val progressMerger: ProgressMerger,
    private val syncScheduler: SyncScheduler
) : SyncRepository {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    override fun observePendingQueueCount(): Flow<Int> = syncQueueDao.observePendingCount()

    override suspend fun fetchRemoteCatalog(): AppResult<List<RemoteCatalogItem>> =
        appwriteRepository.fetchCatalog()

    override suspend fun downloadAndInstallStudy(catalogItem: RemoteCatalogItem): AppResult<Study> {
        val jsonResult = appwriteRepository.downloadStudyJson(catalogItem.storageFileId)
        return when (jsonResult) {
            is AppResult.Failure -> jsonResult
            is AppResult.Success -> {
                // Atomic validation before replacing existing local study
                studyRepository.importValidatedStudyJson(jsonResult.data, isBundledAsset = false)
            }
        }
    }

    override suspend fun syncPendingQueueAndProgress(): AppResult<Int> {
        if (!appwriteRepository.isConfigured) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        val prefs = preferencesDataStore.userPreferences.first()
        val userId = prefs.accountUserId ?: return AppResult.Success(0)

        var syncedCount = 0
        val pending = syncQueueDao.getPendingOperations()
        for (op in pending) {
            if (op.operationType == "UPSERT_PROGRESS") {
                val payloadObj = try {
                    json.decodeFromString<SyncProgressPayload>(op.payload)
                } catch (_: Exception) {
                    syncQueueDao.updateStatus(op.operationId, "DONE", op.retryCount)
                    continue
                }
                val remotePayload = RemoteProgressPayload(
                    userId = userId,
                    studyId = payloadObj.studyId,
                    currentNodeId = payloadObj.currentNodeId,
                    progressJson = op.payload,
                    completed = payloadObj.isCompleted,
                    startedAt = payloadObj.startedAt,
                    completedAt = payloadObj.completedAt,
                    updatedAt = payloadObj.updatedAt
                )
                when (appwriteRepository.upsertProgress(remotePayload)) {
                    is AppResult.Success -> {
                        syncQueueDao.updateStatus(op.operationId, "DONE", op.retryCount)
                        syncedCount++
                    }
                    is AppResult.Failure -> {
                        syncQueueDao.updateStatus(op.operationId, "FAILED", op.retryCount + 1)
                    }
                }
            }
        }
        syncQueueDao.purgeCompleted()

        // Pull remote progress and deterministically merge with Room
        val remoteList = appwriteRepository.listProgress(userId)
        if (remoteList is AppResult.Success) {
            for (remoteRow in remoteList.data) {
                val decoded = try {
                    json.decodeFromString<SyncProgressPayload>(remoteRow.progressJson)
                } catch (_: Exception) {
                    null
                }
                val remoteDomain = if (decoded != null) {
                    StudyProgress(
                        studyId = decoded.studyId,
                        currentNodeId = decoded.currentNodeId,
                        historyJson = decoded.historyJson,
                        answersJson = decoded.answersJson,
                        unlockedNotions = decoded.unlockedNotions,
                        personalConclusion = decoded.personalConclusion,
                        isUnconvinced = decoded.isUnconvinced,
                        isCompleted = decoded.isCompleted,
                        startedAt = decoded.startedAt,
                        lastOpenedAt = decoded.lastOpenedAt,
                        completedAt = decoded.completedAt,
                        updatedAt = decoded.updatedAt
                    )
                } else {
                    StudyProgress(
                        studyId = remoteRow.studyId,
                        currentNodeId = remoteRow.currentNodeId,
                        historyJson = "{}",
                        answersJson = "{}",
                        unlockedNotions = emptyList(),
                        personalConclusion = "",
                        isUnconvinced = false,
                        isCompleted = remoteRow.completed,
                        startedAt = remoteRow.startedAt,
                        lastOpenedAt = remoteRow.updatedAt,
                        completedAt = remoteRow.completedAt,
                        updatedAt = remoteRow.updatedAt
                    )
                }
                val localEntity = progressDao.getProgressForStudy(remoteRow.studyId)
                if (localEntity == null) {
                    progressDao.upsertProgress(remoteDomain.toEntity(progressManager))
                } else {
                    val outcome = progressMerger.merge(
                        local = localEntity.toDomain(progressManager),
                        remote = remoteDomain
                    )
                    progressDao.upsertProgress(outcome.merged.toEntity(progressManager))
                }
            }
        }

        preferencesDataStore.updateSyncPreferences {
            it.copy(lastSyncTimestamp = System.currentTimeMillis())
        }
        return AppResult.Success(syncedCount)
    }

    override suspend fun transferLocalProgressToCloud(): AppResult<Int> {
        if (!appwriteRepository.isConfigured) {
            return AppResult.Failure(AppError.AppwriteUnconfigured)
        }
        val prefs = preferencesDataStore.userPreferences.first()
        val userId = prefs.accountUserId ?: return AppResult.Failure(AppError.SessionExpired)
        val localList = progressDao.getAllProgressSnapshot()
        var uploaded = 0
        for (entity in localList) {
            val domain = entity.toDomain(progressManager)
            val payloadJson = json.encodeToString(
                SyncProgressPayload(
                    studyId = domain.studyId,
                    currentNodeId = domain.currentNodeId,
                    historyJson = domain.historyJson,
                    answersJson = domain.answersJson,
                    unlockedNotions = domain.unlockedNotions,
                    personalConclusion = domain.personalConclusion,
                    isUnconvinced = domain.isUnconvinced,
                    isCompleted = domain.isCompleted,
                    startedAt = domain.startedAt,
                    lastOpenedAt = domain.lastOpenedAt,
                    completedAt = domain.completedAt,
                    updatedAt = domain.updatedAt
                )
            )
            val res = appwriteRepository.upsertProgress(
                RemoteProgressPayload(
                    userId = userId,
                    studyId = domain.studyId,
                    currentNodeId = domain.currentNodeId,
                    progressJson = payloadJson,
                    completed = domain.isCompleted,
                    startedAt = domain.startedAt,
                    completedAt = domain.completedAt,
                    updatedAt = domain.updatedAt
                )
            )
            if (res is AppResult.Success) uploaded++
        }
        return AppResult.Success(uploaded)
    }

    override fun scheduleSyncNow() {
        syncScheduler.enqueueOneTimeSync()
    }

    override fun schedulePeriodicSync() {
        syncScheduler.schedulePeriodicSync()
    }
}

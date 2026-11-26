package com.harimbola.solascriptura.domain.model

import com.harimbola.solascriptura.core.engine.DialogueGraph
import com.harimbola.solascriptura.core.engine.EngineVerseReference

data class Category(
    val categoryId: String,
    val title: String,
    val description: String,
    val sortOrder: Int,
    val icon: String
)

data class VerseDuel(
    val id: String,
    val title: String,
    val passageA: EngineVerseReference,
    val contextA: String,
    val passageB: EngineVerseReference,
    val contextB: String,
    val analysisQuestion: String,
    val suggestedSynthesis: String
)

data class InvestigationClue(
    val id: String,
    val testamentSection: String,
    val verseReference: EngineVerseReference,
    val observationPrompt: String,
    val keyInsight: String
)

data class BibleInvestigation(
    val id: String,
    val centralQuestion: String,
    val clues: List<InvestigationClue>,
    val synthesisPrompt: String
)

data class Study(
    val studyId: String,
    val schemaVersion: Int,
    val contentVersion: Int,
    val locale: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val categoryId: String,
    val tags: List<String>,
    val perspective: String,
    val difficulty: String,
    val estimatedMinutes: Int,
    val editorialStatus: String,
    val prerequisiteStudyIds: List<String>,
    val graph: DialogueGraph,
    val verseDuels: List<VerseDuel> = emptyList(),
    val investigation: BibleInvestigation? = null,
    val isBundledAsset: Boolean = true,
    val downloadedAt: Long = 0L,
    val lastUpdatedAt: Long = 0L
)

enum class NoteType(val labelFr: String, val labelEn: String, val labelMg: String) {
    CONCLUSION("Conclusion", "Conclusion", "Fehin-kevitra"),
    QUESTION("Question", "Question", "Fanontaniana"),
    NOTE("Note", "Note", "Naoty"),
    VERSE("Passage clé", "Key passage", "Andininy"),
    DIFFICULTY("Difficulté", "Difficulty", "Fahasarotana")
}

data class UserNote(
    val id: String,
    val studyId: String,
    val nodeId: String?,
    val noteType: NoteType,
    val title: String,
    val content: String,
    val verseRefLabel: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

data class StudyProgress(
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

data class StudyProgressStats(
    val studiesStarted: Int = 0,
    val studiesCompleted: Int = 0,
    val studiesInProgress: Int = 0,
    val estimatedMinutesStudied: Int = 0,
    val uniqueReferencesExamined: Int = 0,
    val notionsRecorded: Int = 0,
    val totalAvailableStudies: Int = 0
)

data class DailyChallenge(
    val dateIso: String,
    val studyId: String,
    val studyTitle: String,
    val promptText: String,
    val verses: List<EngineVerseReference>,
    val isCompletedToday: Boolean
)

enum class TruthNodeState {
    COMPLETED,
    IN_PROGRESS,
    UNSTARTED
}

data class TruthTreeNode(
    val studyId: String,
    val title: String,
    val categoryId: String,
    val state: TruthNodeState,
    val depth: Int,
    val laneIndex: Int
)

data class TruthTreeEdge(
    val fromStudyId: String,
    val toStudyId: String
)

data class TruthTreeGraph(
    val nodes: List<TruthTreeNode>,
    val edges: List<TruthTreeEdge>
)

data class AppUser(
    val userId: String,
    val email: String,
    val name: String
)

data class RemoteCatalogItem(
    val studyId: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val categoryId: String,
    val locale: String,
    val contentVersion: Int,
    val schemaVersion: Int,
    val storageFileId: String,
    val checksum: String,
    val estimatedMinutes: Int,
    val difficulty: String,
    val editorialStatus: String,
    val published: Boolean
)

package com.harimbola.solascriptura

import com.harimbola.solascriptura.core.engine.DialogueGraph
import com.harimbola.solascriptura.core.engine.DialogueHistory
import com.harimbola.solascriptura.core.engine.DialogueHistoryStep
import com.harimbola.solascriptura.core.engine.DialogueNode
import com.harimbola.solascriptura.core.engine.DialogueNodeType
import com.harimbola.solascriptura.core.engine.DialoguePhase
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.engine.EngineVerseReference
import com.harimbola.solascriptura.core.sync.ProgressMerger
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.TruthNodeState
import com.harimbola.solascriptura.domain.usecase.BuildTruthTreeUseCase
import com.harimbola.solascriptura.domain.usecase.CalculateDailyChallengeUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressMergerAndChallengeTest {

    private val progressManager = DialogueProgressManager()
    private val merger = ProgressMerger(progressManager)
    private val dailyUseCase = CalculateDailyChallengeUseCase()
    private val truthTreeUseCase = BuildTruthTreeUseCase()

    @Test
    fun `progress merger never erases completion and merges distinct steps and conclusions`() {
        val localHistory = DialogueHistory(
            listOf(DialogueHistoryStep("n1", "IRONY", "Q1", "c1", "A", "", 100L))
        )
        val remoteHistory = DialogueHistory(
            listOf(
                DialogueHistoryStep("n1", "IRONY", "Q1", "c1", "A", "", 100L),
                DialogueHistoryStep("n2", "ELENCHUS", "Q2", "c2", "B", "", 200L)
            )
        )

        val local = StudyProgress(
            studyId = "s1",
            currentNodeId = "n3",
            historyJson = progressManager.encodeHistory(localHistory),
            answersJson = progressManager.encodeAnswers(mapOf("n1" to "c1")),
            unlockedNotions = listOf("notion_a"),
            personalConclusion = "Ma note locale.",
            isUnconvinced = false,
            isCompleted = true,
            startedAt = 100L,
            lastOpenedAt = 150L,
            completedAt = 150L,
            updatedAt = 150L
        )
        val remote = StudyProgress(
            studyId = "s1",
            currentNodeId = "n2",
            historyJson = progressManager.encodeHistory(remoteHistory),
            answersJson = progressManager.encodeAnswers(mapOf("n2" to "c2")),
            unlockedNotions = listOf("notion_b"),
            personalConclusion = "Note distante.",
            isUnconvinced = false,
            isCompleted = false,
            startedAt = 120L,
            lastOpenedAt = 250L,
            completedAt = null,
            updatedAt = 250L
        )

        val outcome = merger.merge(local, remote)
        assertTrue(outcome.merged.isCompleted)
        assertTrue(outcome.hasUnresolvedConflict)
        assertEquals(listOf("notion_a", "notion_b"), outcome.merged.unlockedNotions)
        assertEquals(2, progressManager.decodeHistory(outcome.merged.historyJson).steps.size)
        assertTrue(outcome.merged.personalConclusion.contains("Ma note locale."))
        assertTrue(outcome.merged.personalConclusion.contains("Note distante."))
    }

    @Test
    fun `daily challenge is deterministic for a given date seed and supports completion state`() {
        val studies = listOf(
            fakeStudy("a_study", "Étude A"),
            fakeStudy("b_study", "Étude B"),
            fakeStudy("c_study", "Étude C")
        )
        val c1 = dailyUseCase(studies, completedDateIso = null, locale = "fr", yearDaySeed = 42, dateIsoOverride = "2026-02-11")
        val c2 = dailyUseCase(studies, completedDateIso = "2026-02-11", locale = "fr", yearDaySeed = 42, dateIsoOverride = "2026-02-11")

        assertNotNull(c1)
        assertEquals(c1?.studyId, c2?.studyId)
        assertFalse(c1!!.isCompletedToday)
        assertTrue(c2!!.isCompletedToday)
    }

    @Test
    fun `truth tree builds edges from prerequisites and classifies node states`() {
        val s1 = fakeStudy("root", "Racine")
        val s2 = fakeStudy("child", "Enfant", prerequisites = listOf("root"))
        val progress = listOf(
            StudyProgress("root", "n1", "{}", "{}", emptyList(), "", false, true, 1L, 2L, 2L, 2L)
        )
        val tree = truthTreeUseCase(listOf(s1, s2), progress)
        assertEquals(2, tree.nodes.size)
        assertEquals(1, tree.edges.size)
        assertEquals(TruthNodeState.COMPLETED, tree.nodes.first { it.studyId == "root" }.state)
        assertEquals(TruthNodeState.UNSTARTED, tree.nodes.first { it.studyId == "child" }.state)
    }

    private fun fakeStudy(id: String, title: String, prerequisites: List<String> = emptyList()): Study {
        val node = DialogueNode(
            id = "n1",
            phase = DialoguePhase.IRONY,
            type = DialogueNodeType.QUESTION,
            speakerText = "Q",
            verseReferences = listOf(EngineVerseReference("john", "Jean", 3, 16, 16, quotedText = "Car Dieu a tant aimé le monde..."))
        )
        return Study(
            studyId = id,
            schemaVersion = 1,
            contentVersion = 1,
            locale = "fr",
            title = title,
            subtitle = "Sub",
            description = "Desc",
            categoryId = "foundations",
            tags = emptyList(),
            perspective = "SDA",
            difficulty = "beginner",
            estimatedMinutes = 10,
            editorialStatus = "REVIEWED",
            prerequisiteStudyIds = prerequisites,
            graph = DialogueGraph(id, title, "n1", mapOf("n1" to node), emptyMap())
        )
    }
}

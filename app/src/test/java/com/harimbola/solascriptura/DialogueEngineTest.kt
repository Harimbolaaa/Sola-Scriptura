package com.harimbola.solascriptura

import com.harimbola.solascriptura.core.engine.ChoiceKind
import com.harimbola.solascriptura.core.engine.DialogueChoice
import com.harimbola.solascriptura.core.engine.DialogueConclusion
import com.harimbola.solascriptura.core.engine.DialogueEngine
import com.harimbola.solascriptura.core.engine.DialogueGraph
import com.harimbola.solascriptura.core.engine.DialogueNode
import com.harimbola.solascriptura.core.engine.DialogueNodeType
import com.harimbola.solascriptura.core.engine.DialoguePhase
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.engine.DialogueTransition
import com.harimbola.solascriptura.core.engine.EngineVerseReference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DialogueEngineTest {

    private val engine = DialogueEngine()
    private val progressManager = DialogueProgressManager()

    private fun sampleGraph(maxSteps: Int = 60): DialogueGraph {
        val v1 = EngineVerseReference("eccl", "Ecclésiaste", 9, 5, 5, quotedText = "Les morts ne savent rien.")
        val v2 = EngineVerseReference("john", "Jean", 11, 11, 14, quotedText = "Lazare, notre ami, dort.")

        val nodes = listOf(
            DialogueNode(
                id = "n1_irony",
                phase = DialoguePhase.IRONY,
                type = DialogueNodeType.QUESTION,
                speakerText = "Que penses-tu de l'état après la mort ?",
                choices = listOf(
                    DialogueChoice("c_go", "Examiner les textes", nextNodeId = "n2_elenchus"),
                    DialogueChoice("c_ctx", "Voir le contexte", feedback = "Contexte littéraire", nextNodeId = null, kind = ChoiceKind.EXAMINE_CONTEXT, extraVerses = listOf(v1))
                )
            ),
            DialogueNode(
                id = "n2_elenchus",
                phase = DialoguePhase.ELENCHUS,
                type = DialogueNodeType.SCRIPTURE,
                speakerText = "Que dit Ecclésiaste 9:5 ?",
                verseReferences = listOf(v1),
                choices = listOf(
                    DialogueChoice("c_next", "Ils ne savent rien", nextNodeId = "n3_maieutics", unlockedNotion = "inconscience_morts"),
                    DialogueChoice("c_back", "Revenir", nextNodeId = null, kind = ChoiceKind.PREVIOUS_QUESTION),
                    DialogueChoice("c_loop", "Réfléchir encore", nextNodeId = "n1_irony")
                )
            ),
            DialogueNode(
                id = "n3_maieutics",
                phase = DialoguePhase.MAIEUTICS,
                type = DialogueNodeType.COMPARISON,
                speakerText = "Comment Jésus décrit-il la mort dans Jean 11 ?",
                verseReferences = listOf(v2),
                choices = listOf(
                    DialogueChoice("c_synth", "Comme un sommeil", nextNodeId = "n4_synthesis"),
                    DialogueChoice("c_doubt", "Je ne suis pas convaincu", feedback = "Ton doute est respecté.", nextNodeId = "n4_synthesis", kind = ChoiceKind.NOT_CONVINCED)
                )
            ),
            DialogueNode(
                id = "n4_synthesis",
                phase = DialoguePhase.SYNTHESIS,
                type = DialogueNodeType.SYNTHESIS,
                speakerText = "Synthèse finale",
                conclusionId = "concl_1",
                terminal = true
            )
        ).associateBy { it.id }

        val conclusions = mapOf(
            "concl_1" to DialogueConclusion(
                id = "concl_1",
                title = "La mort comme un sommeil",
                summary = "L'Écriture compare la mort à un sommeil jusqu'à la résurrection.",
                verseReferences = listOf(v1, v2)
            )
        )

        return DialogueGraph(
            studyId = "test_study",
            title = "Test",
            startNodeId = "n1_irony",
            nodes = nodes,
            conclusions = conclusions,
            maxSteps = maxSteps
        )
    }

    @Test
    fun `full socratic journey transitions through all phases and tracks progress`() {
        val graph = sampleGraph()
        val s0 = engine.startSession(graph, nowMillis = 100L)
        assertEquals("n1_irony", s0.currentNodeId)

        // Stay with feedback for EXAMINE_CONTEXT
        val (sStay, tStay) = engine.selectChoice(graph, s0, "c_ctx", nowMillis = 110L)
        assertTrue(tStay is DialogueTransition.StayWithFeedback)
        assertEquals("n1_irony", sStay.currentNodeId)

        // IRONY -> ELENCHUS
        val (s1, t1) = engine.selectChoice(graph, sStay, "c_go", nowMillis = 120L)
        assertTrue(t1 is DialogueTransition.MoveToNode)
        assertEquals("n2_elenchus", s1.currentNodeId)
        assertTrue(engine.progressFraction(graph, s1) > engine.progressFraction(graph, s0))

        // ELENCHUS -> MAIEUTICS
        val (s2, _) = engine.selectChoice(graph, s1, "c_next", nowMillis = 130L)
        assertEquals("n3_maieutics", s2.currentNodeId)
        assertTrue("inconscience_morts" in s2.unlockedNotions)

        // MAIEUTICS -> SYNTHESIS with NOT_CONVINCED
        val (s3, _) = engine.selectChoice(graph, s2, "c_doubt", nowMillis = 140L)
        assertEquals("n4_synthesis", s3.currentNodeId)
        assertTrue(s3.isUnconvinced)

        // Complete with personal reflection
        val (sDone, result) = engine.completeWithReflection(
            graph = graph,
            session = s3,
            personalConclusion = "Je souhaite approfondir Jean 11.",
            remainsUnconvinced = true,
            nowMillis = 150L
        )
        assertTrue(sDone.isCompleted)
        assertEquals(1.0f, engine.progressFraction(graph, sDone))
        assertEquals(2, result.examinedVerses.size)
        assertEquals("Je souhaite approfondir Jean 11.", result.personalConclusion)
    }

    @Test
    fun `back navigation pops history and restores previous node`() {
        val graph = sampleGraph()
        val s0 = engine.startSession(graph)
        val (s1, _) = engine.selectChoice(graph, s0, "c_go")
        assertEquals("n2_elenchus", s1.currentNodeId)

        val (sBack, trans) = engine.selectChoice(graph, s1, "c_back")
        assertTrue(trans is DialogueTransition.GoBack)
        assertEquals("n1_irony", sBack.currentNodeId)
        assertEquals(0, sBack.history.steps.size)
    }

    @Test
    fun `step limit protects against infinite cycles`() {
        val graph = sampleGraph(maxSteps = 2)
        var session = engine.startSession(graph)
        session = engine.selectChoice(graph, session, "c_go").first
        session = engine.selectChoice(graph, session, "c_loop").first
        val (bounded, trans) = engine.selectChoice(graph, session, "c_go")
        assertTrue(trans is DialogueTransition.CycleOrLimitFallback)
        assertEquals("n4_synthesis", bounded.currentNodeId)
        assertTrue(bounded.stepLimitReached)
    }

    @Test
    fun `progress manager serializes and restores session accurately`() {
        val graph = sampleGraph()
        val s0 = engine.startSession(graph, nowMillis = 1000L)
        val (s1, _) = engine.selectChoice(graph, s0, "c_go", nowMillis = 1010L)
        val (s2, _) = engine.selectChoice(graph, s1, "c_next", nowMillis = 1020L)

        val historyJson = progressManager.encodeHistory(s2.history)
        val answersJson = progressManager.encodeAnswers(s2.selectedAnswers)
        val notionsJson = progressManager.encodeNotions(s2.unlockedNotions)

        val restored = progressManager.restoreSession(
            graph = graph,
            currentNodeId = s2.currentNodeId,
            historyJson = historyJson,
            answersJson = answersJson,
            notionsJson = notionsJson,
            personalConclusion = "Ma synthèse",
            isUnconvinced = false,
            isCompleted = false,
            startedAtMillis = 1000L,
            updatedAtMillis = 1020L
        )
        assertEquals("n3_maieutics", restored.currentNodeId)
        assertEquals(2, restored.history.steps.size)
        assertEquals(listOf("inconscience_morts"), restored.unlockedNotions)
        assertFalse(restored.isCompleted)
    }
}

package com.harimbola.solascriptura.core.engine

import kotlinx.serialization.Serializable

@Serializable
data class DialogueHistoryStep(
    val nodeId: String,
    val phaseName: String,
    val questionText: String,
    val choiceId: String? = null,
    val choiceLabel: String? = null,
    val feedbackShown: String = "",
    val timestampMillis: Long = 0L
)

@Serializable
data class DialogueHistory(
    val steps: List<DialogueHistoryStep> = emptyList()
) {
    fun append(step: DialogueHistoryStep): DialogueHistory =
        copy(steps = steps + step)

    fun popLast(): Pair<DialogueHistory, DialogueHistoryStep?> {
        if (steps.isEmpty()) return this to null
        return copy(steps = steps.dropLast(1)) to steps.last()
    }

    fun visitedNodeIds(): Set<String> = steps.map { it.nodeId }.toSet()
}

data class DialogueSession(
    val studyId: String,
    val currentNodeId: String,
    val history: DialogueHistory = DialogueHistory(),
    val selectedAnswers: Map<String, String> = emptyMap(),
    val unlockedNotions: List<String> = emptyList(),
    val lastFeedback: String? = null,
    val personalConclusion: String = "",
    val isUnconvinced: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val stepLimitReached: Boolean = false
)

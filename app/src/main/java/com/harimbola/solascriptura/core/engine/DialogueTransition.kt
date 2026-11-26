package com.harimbola.solascriptura.core.engine

sealed interface DialogueTransition {
    data class MoveToNode(
        val targetNodeId: String,
        val feedback: String,
        val unlockedNotion: String? = null
    ) : DialogueTransition

    data class StayWithFeedback(
        val feedback: String,
        val extraVerses: List<EngineVerseReference> = emptyList()
    ) : DialogueTransition

    data class GoBack(
        val previousNodeId: String
    ) : DialogueTransition

    data class Complete(
        val result: DialogueResult
    ) : DialogueTransition

    data class CycleOrLimitFallback(
        val fallbackNodeId: String,
        val reason: String
    ) : DialogueTransition
}

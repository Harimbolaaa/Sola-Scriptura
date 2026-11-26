package com.harimbola.solascriptura.core.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SerializedSessionSnapshot(
    val studyId: String,
    val currentNodeId: String,
    val history: DialogueHistory = DialogueHistory(),
    val selectedAnswers: Map<String, String> = emptyMap(),
    val unlockedNotions: List<String> = emptyList(),
    val personalConclusion: String = "",
    val isUnconvinced: Boolean = false,
    val isCompleted: Boolean = false,
    val startedAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L
)

class DialogueProgressManager(
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
) {
    fun encodeHistory(history: DialogueHistory): String =
        json.encodeToString(history)

    fun decodeHistory(historyJson: String): DialogueHistory = try {
        if (historyJson.isBlank()) DialogueHistory()
        else json.decodeFromString(historyJson)
    } catch (_: Exception) {
        DialogueHistory()
    }

    fun encodeAnswers(answers: Map<String, String>): String =
        json.encodeToString(answers)

    fun decodeAnswers(answersJson: String): Map<String, String> = try {
        if (answersJson.isBlank()) emptyMap()
        else json.decodeFromString(answersJson)
    } catch (_: Exception) {
        emptyMap()
    }

    fun encodeNotions(notions: List<String>): String =
        json.encodeToString(notions)

    fun decodeNotions(notionsJson: String): List<String> = try {
        if (notionsJson.isBlank()) emptyList()
        else json.decodeFromString(notionsJson)
    } catch (_: Exception) {
        emptyList()
    }

    fun restoreSession(
        graph: DialogueGraph,
        currentNodeId: String,
        historyJson: String,
        answersJson: String,
        notionsJson: String,
        personalConclusion: String,
        isUnconvinced: Boolean,
        isCompleted: Boolean,
        startedAtMillis: Long,
        updatedAtMillis: Long
    ): DialogueSession {
        val validNodeId = if (graph.nodeOrNull(currentNodeId) != null) {
            currentNodeId
        } else {
            graph.startNodeId
        }
        return DialogueSession(
            studyId = graph.studyId,
            currentNodeId = validNodeId,
            history = decodeHistory(historyJson),
            selectedAnswers = decodeAnswers(answersJson),
            unlockedNotions = decodeNotions(notionsJson),
            personalConclusion = personalConclusion,
            isUnconvinced = isUnconvinced,
            isCompleted = isCompleted,
            startedAtMillis = startedAtMillis,
            updatedAtMillis = updatedAtMillis
        )
    }
}

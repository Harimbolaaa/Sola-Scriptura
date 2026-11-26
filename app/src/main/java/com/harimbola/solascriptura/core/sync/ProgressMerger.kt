package com.harimbola.solascriptura.core.sync

import com.harimbola.solascriptura.core.engine.DialogueHistory
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.domain.model.StudyProgress
import javax.inject.Inject

/**
 * Deterministic conflict resolution policy for multi-device progress synchronization:
 * 1. Never erase a completed study (isCompleted = true is monotonic).
 * 2. Preserve all valid visited steps from both devices (union by nodeId + choiceId).
 * 3. Merge unlocked doctrinal notions deterministically.
 * 4. Merge non-contradictory selected answers (preserve both if distinct keys; prefer most recent update for same key).
 * 5. Keep the most recent active branch (currentNodeId) unless one is completed and the other isn't.
 * 6. Preserve personal conclusion if non-empty (combine distinct entries if both devices edited).
 */
class ProgressMerger @Inject constructor(
    private val progressManager: DialogueProgressManager
) {
    data class MergeOutcome(
        val merged: StudyProgress,
        val hasUnresolvedConflict: Boolean = false
    )

    fun merge(local: StudyProgress, remote: StudyProgress): MergeOutcome {
        require(local.studyId == remote.studyId) { "Cannot merge progress for different studies." }

        val localHistory = progressManager.decodeHistory(local.historyJson)
        val remoteHistory = progressManager.decodeHistory(remote.historyJson)

        val mergedSteps = (localHistory.steps + remoteHistory.steps)
            .distinctBy { "${it.nodeId}:${it.choiceId ?: ""}" }
            .sortedBy { it.timestampMillis }
        val mergedHistoryJson = progressManager.encodeHistory(DialogueHistory(mergedSteps))

        val localAnswers = progressManager.decodeAnswers(local.answersJson)
        val remoteAnswers = progressManager.decodeAnswers(remote.answersJson)

        val preferRemote = remote.updatedAt >= local.updatedAt
        val mergedAnswers = if (preferRemote) {
            localAnswers + remoteAnswers
        } else {
            remoteAnswers + localAnswers
        }
        val mergedAnswersJson = progressManager.encodeAnswers(mergedAnswers)

        val mergedNotions = (local.unlockedNotions + remote.unlockedNotions).distinct()

        val completed = local.isCompleted || remote.isCompleted
        val completedAt = listOfNotNull(local.completedAt, remote.completedAt).minOrNull()
        val startedAt = minOf(local.startedAt, remote.startedAt)
        val lastOpenedAt = maxOf(local.lastOpenedAt, remote.lastOpenedAt)
        val updatedAt = maxOf(local.updatedAt, remote.updatedAt)

        val activeNodeId = when {
            local.isCompleted && !remote.isCompleted -> local.currentNodeId
            remote.isCompleted && !local.isCompleted -> remote.currentNodeId
            preferRemote -> remote.currentNodeId
            else -> local.currentNodeId
        }

        val (mergedConclusion, conflict) = mergeConclusions(
            localText = local.personalConclusion,
            remoteText = remote.personalConclusion,
            preferRemote = preferRemote
        )

        val merged = StudyProgress(
            studyId = local.studyId,
            currentNodeId = activeNodeId,
            historyJson = mergedHistoryJson,
            answersJson = mergedAnswersJson,
            unlockedNotions = mergedNotions,
            personalConclusion = mergedConclusion,
            isUnconvinced = if (preferRemote) remote.isUnconvinced else local.isUnconvinced,
            isCompleted = completed,
            startedAt = startedAt,
            lastOpenedAt = lastOpenedAt,
            completedAt = completedAt,
            updatedAt = updatedAt
        )
        return MergeOutcome(merged = merged, hasUnresolvedConflict = conflict)
    }

    private fun mergeConclusions(
        localText: String,
        remoteText: String,
        preferRemote: Boolean
    ): Pair<String, Boolean> {
        val l = localText.trim()
        val r = remoteText.trim()
        return when {
            l.isEmpty() -> r to false
            r.isEmpty() -> l to false
            l == r -> l to false
            r.contains(l) -> r to false
            l.contains(r) -> l to false
            else -> {
                val combined = if (preferRemote) {
                    "$r\n\n---\n(Version locale conservée)\n$l"
                } else {
                    "$l\n\n---\n(Version distante conservée)\n$r"
                }
                combined to true
            }
        }
    }
}

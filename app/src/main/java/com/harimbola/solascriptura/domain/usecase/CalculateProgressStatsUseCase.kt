package com.harimbola.solascriptura.domain.usecase

import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.StudyProgressStats
import com.harimbola.solascriptura.domain.model.UserNote
import javax.inject.Inject

class CalculateProgressStatsUseCase @Inject constructor() {

    operator fun invoke(
        studies: List<Study>,
        progressList: List<StudyProgress>,
        notes: List<UserNote>,
        visitedNodesCount: Int
    ): StudyProgressStats {
        val studyMap = studies.associateBy { it.studyId }
        val started = progressList.size
        val completed = progressList.count { it.isCompleted }
        val inProgress = progressList.count { !it.isCompleted }

        val estimatedMinutes = progressList.sumOf { progress ->
            val studyMinutes = studyMap[progress.studyId]?.estimatedMinutes ?: 10
            if (progress.isCompleted) studyMinutes else (studyMinutes / 2).coerceAtLeast(2)
        }

        val uniqueNotions = progressList
            .flatMap { it.unlockedNotions }
            .distinct()
            .size + notes.size

        val versesExamined = progressList.sumOf { progress ->
            val study = studyMap[progress.studyId] ?: return@sumOf 0
            study.graph.nodes.values
                .filter { node ->
                    node.id == progress.currentNodeId || progress.historyJson.contains(node.id)
                }
                .flatMap { it.verseReferences }
                .distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}-${it.endVerse}" }
                .size
        }.coerceAtLeast(visitedNodesCount)

        return StudyProgressStats(
            studiesStarted = started,
            studiesCompleted = completed,
            studiesInProgress = inProgress,
            estimatedMinutesStudied = estimatedMinutes,
            uniqueReferencesExamined = versesExamined,
            notionsRecorded = uniqueNotions,
            totalAvailableStudies = studies.size
        )
    }
}

package com.harimbola.solascriptura.domain.usecase

import com.harimbola.solascriptura.core.common.AppConstants
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.TruthNodeState
import com.harimbola.solascriptura.domain.model.TruthTreeEdge
import com.harimbola.solascriptura.domain.model.TruthTreeGraph
import com.harimbola.solascriptura.domain.model.TruthTreeNode
import javax.inject.Inject

class BuildTruthTreeUseCase @Inject constructor() {

    operator fun invoke(
        studies: List<Study>,
        progressList: List<StudyProgress>
    ): TruthTreeGraph {
        if (studies.isEmpty()) return TruthTreeGraph(emptyList(), emptyList())

        val progressMap = progressList.associateBy { it.studyId }
        val studyMap = studies.associateBy { it.studyId }
        val edges = mutableListOf<TruthTreeEdge>()
        val edgeKeys = mutableSetOf<String>()

        // 1. Explicit prerequisite edges from JSON
        studies.forEach { study ->
            study.prerequisiteStudyIds.forEach { prereqId ->
                if (prereqId in studyMap && prereqId != study.studyId) {
                    val key = "$prereqId->${study.studyId}"
                    if (edgeKeys.add(key)) {
                        edges.add(TruthTreeEdge(prereqId, study.studyId))
                    }
                }
            }
            // 2. Explicit relatedStudyIds in conclusions
            study.graph.conclusions.values.forEach { conclusion ->
                conclusion.relatedStudyIds.forEach { relatedId ->
                    if (relatedId in studyMap && relatedId != study.studyId) {
                        val reverseKey = "$relatedId->${study.studyId}"
                        val key = "${study.studyId}->$relatedId"
                        if (reverseKey !in edgeKeys && edgeKeys.add(key)) {
                            edges.add(TruthTreeEdge(study.studyId, relatedId))
                        }
                    }
                }
            }
        }

        // 3. Fallback category progression if a study has no explicit links
        val orderedCategories = AppConstants.STUDY_CATEGORIES_ORDER
        val byCategory = studies.groupBy { it.categoryId }
        for (i in 0 until orderedCategories.size - 1) {
            val currentCat = byCategory[orderedCategories[i]].orEmpty()
            val nextCat = byCategory[orderedCategories[i + 1]].orEmpty()
            val parent = currentCat.firstOrNull()
            val child = nextCat.firstOrNull()
            if (parent != null && child != null) {
                val hasIncoming = edges.any { it.toStudyId == child.studyId }
                if (!hasIncoming) {
                    val key = "${parent.studyId}->${child.studyId}"
                    if (edgeKeys.add(key)) {
                        edges.add(TruthTreeEdge(parent.studyId, child.studyId))
                    }
                }
            }
        }

        // Assign depths from category rank + dependency topological depth
        val categoryDepth = orderedCategories.withIndex().associate { it.value to it.index }
        val depthMap = mutableMapOf<String, Int>()
        studies.forEach { study ->
            depthMap[study.studyId] = categoryDepth[study.categoryId] ?: 0
        }
        repeat(studies.size) {
            edges.forEach { edge ->
                val fromD = depthMap[edge.fromStudyId] ?: 0
                val toD = depthMap[edge.toStudyId] ?: 0
                if (toD <= fromD) {
                    depthMap[edge.toStudyId] = fromD + 1
                }
            }
        }

        val groupedByDepth = studies.groupBy { depthMap[it.studyId] ?: 0 }
        val nodes = mutableListOf<TruthTreeNode>()
        groupedByDepth.toSortedMap().forEach { (depth, depthStudies) ->
            depthStudies.forEachIndexed { laneIdx, study ->
                val progress = progressMap[study.studyId]
                val state = when {
                    progress?.isCompleted == true -> TruthNodeState.COMPLETED
                    progress != null -> TruthNodeState.IN_PROGRESS
                    else -> TruthNodeState.UNSTARTED
                }
                nodes.add(
                    TruthTreeNode(
                        studyId = study.studyId,
                        title = study.title,
                        categoryId = study.categoryId,
                        state = state,
                        depth = depth,
                        laneIndex = laneIdx
                    )
                )
            }
        }

        return TruthTreeGraph(nodes = nodes, edges = edges)
    }
}

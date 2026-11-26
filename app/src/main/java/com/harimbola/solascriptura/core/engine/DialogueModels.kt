package com.harimbola.solascriptura.core.engine

import kotlinx.serialization.Serializable

enum class DialoguePhase {
    IRONY,
    ELENCHUS,
    MAIEUTICS,
    SYNTHESIS,
    REFLECTION
}

enum class DialogueNodeType {
    QUESTION,
    SCRIPTURE,
    COMPARISON,
    REFLECTION,
    SYNTHESIS,
    END
}

enum class ChoiceKind {
    STANDARD,
    NOT_CONVINCED,
    EXAMINE_CONTEXT,
    COMPARE_PASSAGES,
    PREVIOUS_QUESTION,
    DONT_KNOW
}

@Serializable
data class EngineVerseReference(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int = startVerse,
    val translationId: String = "LSG",
    val quotedText: String = "",
    val licenseId: String = "PUBLIC_DOMAIN",
    val contextSummary: String = ""
) {
    fun formattedReference(): String = if (endVerse > startVerse) {
        "$bookName $chapter:$startVerse-$endVerse"
    } else {
        "$bookName $chapter:$startVerse"
    }
}

data class DialogueChoice(
    val id: String,
    val label: String,
    val feedback: String = "",
    val nextNodeId: String? = null,
    val kind: ChoiceKind = ChoiceKind.STANDARD,
    val unlockedNotion: String? = null,
    val extraVerses: List<EngineVerseReference> = emptyList()
)

data class DialogueNode(
    val id: String,
    val phase: DialoguePhase,
    val type: DialogueNodeType,
    val speakerText: String,
    val contextText: String = "",
    val instructionalText: String = "",
    val proposedInterpretation: String = "",
    val verseReferences: List<EngineVerseReference> = emptyList(),
    val choices: List<DialogueChoice> = emptyList(),
    val conclusionId: String? = null,
    val terminal: Boolean = false,
    val allowBackNavigation: Boolean = true
)

data class DialogueConclusion(
    val id: String,
    val title: String,
    val summary: String,
    val reasoningSteps: List<String> = emptyList(),
    val verseReferences: List<EngineVerseReference> = emptyList(),
    val reflectionQuestion: String = "",
    val relatedStudyIds: List<String> = emptyList()
)

data class DialogueGraph(
    val studyId: String,
    val title: String,
    val startNodeId: String,
    val nodes: Map<String, DialogueNode>,
    val conclusions: Map<String, DialogueConclusion>,
    val maxSteps: Int = 60
) {
    fun nodeOrNull(nodeId: String): DialogueNode? = nodes[nodeId]

    fun conclusionOrNull(conclusionId: String?): DialogueConclusion? =
        conclusionId?.let { conclusions[it] }

    /**
     * Estimates depth from startNodeId to a terminal/synthesis node for progress indication.
     */
    fun estimatedTotalSteps(): Int {
        val visited = mutableSetOf(startNodeId)
        val queue = ArrayDeque<Pair<String, Int>>()
        queue.add(startNodeId to 1)
        var maxDist = 1
        while (queue.isNotEmpty()) {
            val (id, d) = queue.removeFirst()
            val node = nodes[id] ?: continue
            if (node.terminal || node.type == DialogueNodeType.END || node.type == DialogueNodeType.SYNTHESIS) {
                if (d > maxDist) maxDist = d
            }
            node.choices.forEach { choice ->
                val next = choice.nextNodeId
                if (next != null && visited.add(next)) {
                    queue.add(next to d + 1)
                }
            }
        }
        return maxDist.coerceAtLeast(1)
    }
}

data class DialogueResult(
    val studyId: String,
    val conclusion: DialogueConclusion?,
    val examinedVerses: List<EngineVerseReference>,
    val unlockedNotions: List<String>,
    val personalConclusion: String,
    val isUnconvinced: Boolean,
    val stepsCompleted: Int
)

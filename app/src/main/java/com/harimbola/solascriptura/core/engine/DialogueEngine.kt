package com.harimbola.solascriptura.core.engine

class DialogueEngine {

    fun startSession(
        graph: DialogueGraph,
        nowMillis: Long = System.currentTimeMillis()
    ): DialogueSession = DialogueSession(
        studyId = graph.studyId,
        currentNodeId = graph.startNodeId,
        history = DialogueHistory(),
        selectedAnswers = emptyMap(),
        unlockedNotions = emptyList(),
        lastFeedback = null,
        personalConclusion = "",
        isUnconvinced = false,
        isCompleted = false,
        startedAtMillis = nowMillis,
        updatedAtMillis = nowMillis
    )

    fun currentNode(graph: DialogueGraph, session: DialogueSession): DialogueNode? =
        graph.nodeOrNull(session.currentNodeId)

    fun selectChoice(
        graph: DialogueGraph,
        session: DialogueSession,
        choiceId: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<DialogueSession, DialogueTransition> {
        val node = graph.nodeOrNull(session.currentNodeId)
            ?: return session to DialogueTransition.Complete(buildResult(graph, session))

        val choice = node.choices.firstOrNull { it.id == choiceId }
            ?: return session to DialogueTransition.StayWithFeedback("")

        if (choice.kind == ChoiceKind.PREVIOUS_QUESTION) {
            return goBack(graph, session, nowMillis)
        }

        // Guard against excessively long paths or infinite loops
        if (session.history.steps.size >= graph.maxSteps) {
            val synthesisNode = findSynthesisOrTerminalNode(graph)
            if (synthesisNode != null && synthesisNode.id != session.currentNodeId) {
                val boundedSession = session.copy(
                    currentNodeId = synthesisNode.id,
                    lastFeedback = "Parcours orienté vers la synthèse afin de préserver la clarté de l'étude.",
                    updatedAtMillis = nowMillis,
                    stepLimitReached = true
                )
                return boundedSession to DialogueTransition.CycleOrLimitFallback(
                    fallbackNodeId = synthesisNode.id,
                    reason = "Step limit reached"
                )
            }
            val completedSession = session.copy(
                isCompleted = true,
                updatedAtMillis = nowMillis,
                stepLimitReached = true
            )
            return completedSession to DialogueTransition.Complete(buildResult(graph, completedSession))
        }

        val updatedAnswers = session.selectedAnswers + (node.id to choice.id)
        val updatedNotions = if (choice.unlockedNotion != null && choice.unlockedNotion !in session.unlockedNotions) {
            session.unlockedNotions + choice.unlockedNotion
        } else {
            session.unlockedNotions
        }
        val updatedUnconvinced = session.isUnconvinced || choice.kind == ChoiceKind.NOT_CONVINCED

        val step = DialogueHistoryStep(
            nodeId = node.id,
            phaseName = node.phase.name,
            questionText = node.speakerText,
            choiceId = choice.id,
            choiceLabel = choice.label,
            feedbackShown = choice.feedback,
            timestampMillis = nowMillis
        )

        val targetId = choice.nextNodeId
        if (targetId == null) {
            // Stay on node with feedback (e.g. "Examine context" or "Not convinced" without branch) or finish if terminal
            return if (node.terminal || node.type == DialogueNodeType.SYNTHESIS || node.type == DialogueNodeType.END) {
                val completedSession = session.copy(
                    history = session.history.append(step),
                    selectedAnswers = updatedAnswers,
                    unlockedNotions = updatedNotions,
                    isUnconvinced = updatedUnconvinced,
                    isCompleted = true,
                    lastFeedback = choice.feedback.ifBlank { null },
                    updatedAtMillis = nowMillis
                )
                completedSession to DialogueTransition.Complete(buildResult(graph, completedSession))
            } else {
                val stayedSession = session.copy(
                    selectedAnswers = updatedAnswers,
                    unlockedNotions = updatedNotions,
                    isUnconvinced = updatedUnconvinced,
                    lastFeedback = choice.feedback.ifBlank { null },
                    updatedAtMillis = nowMillis
                )
                stayedSession to DialogueTransition.StayWithFeedback(
                    feedback = choice.feedback,
                    extraVerses = choice.extraVerses
                )
            }
        }

        val targetNode = graph.nodeOrNull(targetId)
        if (targetNode == null) {
            val completedSession = session.copy(
                history = session.history.append(step),
                selectedAnswers = updatedAnswers,
                unlockedNotions = updatedNotions,
                isUnconvinced = updatedUnconvinced,
                isCompleted = true,
                updatedAtMillis = nowMillis
            )
            return completedSession to DialogueTransition.Complete(buildResult(graph, completedSession))
        }

        // Loop protection: if target node was already visited more than twice, route gracefully to synthesis
        val visitCount = session.history.steps.count { it.nodeId == targetId }
        if (visitCount >= 2 && targetId != node.id) {
            val synthesisNode = findSynthesisOrTerminalNode(graph)
            if (synthesisNode != null) {
                val redirected = session.copy(
                    currentNodeId = synthesisNode.id,
                    history = session.history.append(step),
                    selectedAnswers = updatedAnswers,
                    unlockedNotions = updatedNotions,
                    isUnconvinced = updatedUnconvinced,
                    lastFeedback = choice.feedback.ifBlank { null },
                    updatedAtMillis = nowMillis
                )
                return redirected to DialogueTransition.CycleOrLimitFallback(
                    fallbackNodeId = synthesisNode.id,
                    reason = "Repeated cycle detected"
                )
            }
        }

        val nextSession = session.copy(
            currentNodeId = targetNode.id,
            history = session.history.append(step),
            selectedAnswers = updatedAnswers,
            unlockedNotions = updatedNotions,
            isUnconvinced = updatedUnconvinced,
            lastFeedback = choice.feedback.ifBlank { null },
            updatedAtMillis = nowMillis
        )
        return nextSession to DialogueTransition.MoveToNode(
            targetNodeId = targetNode.id,
            feedback = choice.feedback,
            unlockedNotion = choice.unlockedNotion
        )
    }

    fun goBack(
        graph: DialogueGraph,
        session: DialogueSession,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<DialogueSession, DialogueTransition> {
        val current = graph.nodeOrNull(session.currentNodeId)
        if (current != null && !current.allowBackNavigation) {
            return session to DialogueTransition.StayWithFeedback("")
        }
        val (trimmedHistory, previousStep) = session.history.popLast()
        if (previousStep == null) {
            return session to DialogueTransition.StayWithFeedback("")
        }
        val restored = session.copy(
            currentNodeId = previousStep.nodeId,
            history = trimmedHistory,
            lastFeedback = null,
            isCompleted = false,
            updatedAtMillis = nowMillis
        )
        return restored to DialogueTransition.GoBack(previousStep.nodeId)
    }

    fun completeWithReflection(
        graph: DialogueGraph,
        session: DialogueSession,
        personalConclusion: String,
        remainsUnconvinced: Boolean,
        nowMillis: Long = System.currentTimeMillis()
    ): Pair<DialogueSession, DialogueResult> {
        val updated = session.copy(
            personalConclusion = personalConclusion,
            isUnconvinced = remainsUnconvinced || session.isUnconvinced,
            isCompleted = true,
            updatedAtMillis = nowMillis
        )
        return updated to buildResult(graph, updated)
    }

    fun progressFraction(graph: DialogueGraph, session: DialogueSession): Float {
        if (session.isCompleted) return 1.0f
        val current = graph.nodeOrNull(session.currentNodeId)
        if (current != null && (current.terminal || current.type == DialogueNodeType.SYNTHESIS || current.type == DialogueNodeType.END)) {
            return 0.95f
        }
        val estTotal = graph.estimatedTotalSteps().coerceAtLeast(2)
        val visited = (session.history.visitedNodeIds().size + 1).toFloat()
        return (visited / estTotal.toFloat()).coerceIn(0.08f, 0.92f)
    }

    fun buildResult(graph: DialogueGraph, session: DialogueSession): DialogueResult {
        val current = graph.nodeOrNull(session.currentNodeId)
        val conclusion = graph.conclusionOrNull(current?.conclusionId)
            ?: graph.conclusions.values.firstOrNull()

        val verses = buildList {
            session.history.steps.forEach { step ->
                graph.nodeOrNull(step.nodeId)?.verseReferences?.let { addAll(it) }
            }
            current?.verseReferences?.let { addAll(it) }
            conclusion?.verseReferences?.let { addAll(it) }
        }.distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}-${it.endVerse}" }

        return DialogueResult(
            studyId = graph.studyId,
            conclusion = conclusion,
            examinedVerses = verses,
            unlockedNotions = session.unlockedNotions,
            personalConclusion = session.personalConclusion,
            isUnconvinced = session.isUnconvinced,
            stepsCompleted = session.history.steps.size + 1
        )
    }

    private fun findSynthesisOrTerminalNode(graph: DialogueGraph): DialogueNode? =
        graph.nodes.values.firstOrNull {
            it.type == DialogueNodeType.SYNTHESIS || it.phase == DialoguePhase.SYNTHESIS
        } ?: graph.nodes.values.firstOrNull { it.terminal || it.type == DialogueNodeType.END }
}

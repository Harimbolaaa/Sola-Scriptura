package com.harimbola.solascriptura.data.mapper

import com.harimbola.solascriptura.core.database.entity.CategoryEntity
import com.harimbola.solascriptura.core.database.entity.DialogueProgressEntity
import com.harimbola.solascriptura.core.database.entity.StudyEntity
import com.harimbola.solascriptura.core.database.entity.UserNoteEntity
import com.harimbola.solascriptura.core.engine.ChoiceKind
import com.harimbola.solascriptura.core.engine.DialogueChoice
import com.harimbola.solascriptura.core.engine.DialogueConclusion
import com.harimbola.solascriptura.core.engine.DialogueGraph
import com.harimbola.solascriptura.core.engine.DialogueNode
import com.harimbola.solascriptura.core.engine.DialogueNodeType
import com.harimbola.solascriptura.core.engine.DialoguePhase
import com.harimbola.solascriptura.core.engine.DialogueProgressManager
import com.harimbola.solascriptura.core.engine.EngineVerseReference
import com.harimbola.solascriptura.core.json.DialogueChoiceDto
import com.harimbola.solascriptura.core.json.DialogueNodeDto
import com.harimbola.solascriptura.core.json.DialogueNodeTypeDto
import com.harimbola.solascriptura.core.json.DialoguePhaseDto
import com.harimbola.solascriptura.core.json.StudyConclusionDto
import com.harimbola.solascriptura.core.json.StudyDocumentDto
import com.harimbola.solascriptura.core.json.StudyJsonConfig
import com.harimbola.solascriptura.core.json.VerseReferenceDto
import com.harimbola.solascriptura.domain.model.BibleInvestigation
import com.harimbola.solascriptura.domain.model.Category
import com.harimbola.solascriptura.domain.model.InvestigationClue
import com.harimbola.solascriptura.domain.model.NoteType
import com.harimbola.solascriptura.domain.model.Study
import com.harimbola.solascriptura.domain.model.StudyProgress
import com.harimbola.solascriptura.domain.model.UserNote
import com.harimbola.solascriptura.domain.model.VerseDuel

fun CategoryEntity.toDomain(): Category = Category(
    categoryId = categoryId,
    title = title,
    description = description,
    sortOrder = sortOrder,
    icon = icon
)

fun VerseReferenceDto.toEngine(): EngineVerseReference = EngineVerseReference(
    bookId = bookId,
    bookName = bookName,
    chapter = chapter,
    startVerse = startVerse,
    endVerse = endVerse,
    translationId = translationId,
    quotedText = quotedText,
    licenseId = licenseId,
    contextSummary = contextSummary
)

fun DialoguePhaseDto.toEngine(): DialoguePhase = when (this) {
    DialoguePhaseDto.IRONY -> DialoguePhase.IRONY
    DialoguePhaseDto.ELENCHUS -> DialoguePhase.ELENCHUS
    DialoguePhaseDto.MAIEUTICS -> DialoguePhase.MAIEUTICS
    DialoguePhaseDto.SYNTHESIS -> DialoguePhase.SYNTHESIS
    DialoguePhaseDto.REFLECTION -> DialoguePhase.REFLECTION
    DialoguePhaseDto.UNKNOWN -> DialoguePhase.MAIEUTICS
}

fun DialogueNodeTypeDto.toEngine(): DialogueNodeType = when (this) {
    DialogueNodeTypeDto.QUESTION -> DialogueNodeType.QUESTION
    DialogueNodeTypeDto.SCRIPTURE -> DialogueNodeType.SCRIPTURE
    DialogueNodeTypeDto.COMPARISON -> DialogueNodeType.COMPARISON
    DialogueNodeTypeDto.REFLECTION -> DialogueNodeType.REFLECTION
    DialogueNodeTypeDto.SYNTHESIS -> DialogueNodeType.SYNTHESIS
    DialogueNodeTypeDto.END -> DialogueNodeType.END
    DialogueNodeTypeDto.UNKNOWN -> DialogueNodeType.QUESTION
}

fun DialogueChoiceDto.toEngine(): DialogueChoice = DialogueChoice(
    id = id,
    label = label,
    feedback = feedback,
    nextNodeId = nextNodeId,
    kind = ChoiceKind.entries.firstOrNull { it.name == kind.uppercase() } ?: ChoiceKind.STANDARD,
    unlockedNotion = unlockedNotion,
    extraVerses = extraVerses.map { it.toEngine() }
)

fun DialogueNodeDto.toEngine(): DialogueNode = DialogueNode(
    id = id,
    phase = phase.toEngine(),
    type = type.toEngine(),
    speakerText = speakerText,
    contextText = contextText,
    instructionalText = instructionalText,
    proposedInterpretation = proposedInterpretation,
    verseReferences = verseReferences.map { it.toEngine() },
    choices = choices.map { it.toEngine() },
    conclusionId = conclusionId,
    terminal = terminal,
    allowBackNavigation = allowBackNavigation
)

fun StudyConclusionDto.toEngine(): DialogueConclusion = DialogueConclusion(
    id = id,
    title = title,
    summary = summary,
    reasoningSteps = reasoningSteps,
    verseReferences = verseReferences.map { it.toEngine() },
    reflectionQuestion = reflectionQuestion,
    relatedStudyIds = relatedStudyIds
)

fun StudyDocumentDto.toGraph(): DialogueGraph = DialogueGraph(
    studyId = studyId,
    title = title,
    startNodeId = startNodeId,
    nodes = nodes.associate { it.id to it.toEngine() },
    conclusions = conclusions.associate { it.id to it.toEngine() }
)

fun StudyDocumentDto.toEntity(
    rawJson: String,
    checksum: String,
    isBundledAsset: Boolean,
    nowMillis: Long
): StudyEntity = StudyEntity(
    id = "${studyId}_${locale}",
    studyId = studyId,
    schemaVersion = schemaVersion,
    contentVersion = contentVersion,
    locale = locale,
    title = title,
    subtitle = subtitle,
    categoryId = categoryId,
    description = description,
    difficulty = difficulty,
    estimatedMinutes = estimatedMinutes,
    tagsJson = StudyJsonConfig.json.encodeToString(tags),
    perspective = perspective,
    jsonContent = rawJson,
    checksum = checksum,
    editorialStatus = editorialStatus,
    downloadedAt = nowMillis,
    lastUpdatedAt = nowMillis,
    isAvailable = true,
    isBundledAsset = isBundledAsset
)

fun StudyEntity.toDomain(): Study? {
    val doc = try {
        StudyJsonConfig.json.decodeFromString<StudyDocumentDto>(jsonContent)
    } catch (_: Exception) {
        return null
    }
    return Study(
        studyId = doc.studyId.ifBlank { studyId },
        schemaVersion = doc.schemaVersion,
        contentVersion = doc.contentVersion,
        locale = doc.locale,
        title = doc.title,
        subtitle = doc.subtitle,
        description = doc.description,
        categoryId = doc.categoryId,
        tags = doc.tags,
        perspective = doc.perspective,
        difficulty = doc.difficulty,
        estimatedMinutes = doc.estimatedMinutes,
        editorialStatus = doc.editorialStatus,
        prerequisiteStudyIds = doc.prerequisiteStudyIds,
        graph = doc.toGraph(),
        verseDuels = doc.verseDuels.map { duel ->
            VerseDuel(
                id = duel.id,
                title = duel.title,
                passageA = duel.passageA.toEngine(),
                contextA = duel.contextA,
                passageB = duel.passageB.toEngine(),
                contextB = duel.contextB,
                analysisQuestion = duel.analysisQuestion,
                suggestedSynthesis = duel.suggestedSynthesis
            )
        },
        investigation = doc.investigation?.let { inv ->
            BibleInvestigation(
                id = inv.id,
                centralQuestion = inv.centralQuestion,
                clues = inv.clues.map { clue ->
                    InvestigationClue(
                        id = clue.id,
                        testamentSection = clue.testamentSection,
                        verseReference = clue.verseReference.toEngine(),
                        observationPrompt = clue.observationPrompt,
                        keyInsight = clue.keyInsight
                    )
                },
                synthesisPrompt = inv.synthesisPrompt
            )
        },
        isBundledAsset = isBundledAsset,
        downloadedAt = downloadedAt,
        lastUpdatedAt = lastUpdatedAt
    )
}

fun DialogueProgressEntity.toDomain(progressManager: DialogueProgressManager): StudyProgress =
    StudyProgress(
        studyId = studyId,
        currentNodeId = currentNodeId,
        historyJson = historyJson,
        answersJson = answersJson,
        unlockedNotions = progressManager.decodeNotions(unlockedNotionsJson),
        personalConclusion = personalConclusion,
        isUnconvinced = isUnconvinced,
        isCompleted = isCompleted,
        startedAt = startedAt,
        lastOpenedAt = lastOpenedAt,
        completedAt = completedAt,
        updatedAt = updatedAt
    )

fun StudyProgress.toEntity(progressManager: DialogueProgressManager): DialogueProgressEntity =
    DialogueProgressEntity(
        id = "prog_$studyId",
        studyId = studyId,
        currentNodeId = currentNodeId,
        historyJson = historyJson,
        answersJson = answersJson,
        unlockedNotionsJson = progressManager.encodeNotions(unlockedNotions),
        personalConclusion = personalConclusion,
        isUnconvinced = isUnconvinced,
        isCompleted = isCompleted,
        startedAt = startedAt,
        lastOpenedAt = lastOpenedAt,
        completedAt = completedAt,
        updatedAt = updatedAt
    )

fun UserNoteEntity.toDomain(): UserNote = UserNote(
    id = id,
    studyId = studyId,
    nodeId = nodeId,
    noteType = NoteType.entries.firstOrNull { it.name == noteType } ?: NoteType.NOTE,
    title = title,
    content = content,
    verseRefLabel = verseRefLabel,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserNote.toEntity(): UserNoteEntity = UserNoteEntity(
    id = id,
    studyId = studyId,
    nodeId = nodeId,
    noteType = noteType.name,
    title = title,
    content = content,
    verseRefLabel = verseRefLabel,
    createdAt = createdAt,
    updatedAt = updatedAt
)

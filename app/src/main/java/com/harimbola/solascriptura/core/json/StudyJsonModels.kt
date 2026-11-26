package com.harimbola.solascriptura.core.json

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

object StudyJsonConfig {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
        encodeDefaults = true
        coerceInputValues = true
    }
}

@Serializable(with = DialoguePhaseSerializer::class)
enum class DialoguePhaseDto {
    IRONY,
    ELENCHUS,
    MAIEUTICS,
    SYNTHESIS,
    REFLECTION,
    UNKNOWN
}

object DialoguePhaseSerializer : KSerializer<DialoguePhaseDto> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DialoguePhaseDto", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DialoguePhaseDto) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): DialoguePhaseDto {
        val raw = decoder.decodeString().uppercase()
        return DialoguePhaseDto.entries.firstOrNull { it.name == raw } ?: DialoguePhaseDto.UNKNOWN
    }
}

@Serializable(with = DialogueNodeTypeSerializer::class)
enum class DialogueNodeTypeDto {
    QUESTION,
    SCRIPTURE,
    COMPARISON,
    REFLECTION,
    SYNTHESIS,
    END,
    UNKNOWN
}

object DialogueNodeTypeSerializer : KSerializer<DialogueNodeTypeDto> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DialogueNodeTypeDto", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: DialogueNodeTypeDto) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): DialogueNodeTypeDto {
        val raw = decoder.decodeString().uppercase()
        return DialogueNodeTypeDto.entries.firstOrNull { it.name == raw } ?: DialogueNodeTypeDto.UNKNOWN
    }
}

@Serializable
data class StudyMetadataDto(
    val author: String = "",
    val reviewer: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
    val license: String = "",
    val bibliography: List<String> = emptyList()
)

@Serializable
data class VerseReferenceDto(
    val bookId: String,
    val bookName: String,
    val chapter: Int,
    val startVerse: Int,
    val endVerse: Int = startVerse,
    val translationId: String = "LSG",
    val quotedText: String = "",
    val licenseId: String = "PUBLIC_DOMAIN",
    val contextSummary: String = ""
)

@Serializable
data class DialogueChoiceDto(
    val id: String,
    val label: String,
    val feedback: String = "",
    val nextNodeId: String? = null,
    val kind: String = "STANDARD",
    val unlockedNotion: String? = null,
    val extraVerses: List<VerseReferenceDto> = emptyList()
)

@Serializable
data class DialogueNodeDto(
    val id: String,
    val phase: DialoguePhaseDto,
    val type: DialogueNodeTypeDto,
    val speakerText: String,
    val contextText: String = "",
    val instructionalText: String = "",
    val proposedInterpretation: String = "",
    val verseReferences: List<VerseReferenceDto> = emptyList(),
    val choices: List<DialogueChoiceDto> = emptyList(),
    val conclusionId: String? = null,
    val terminal: Boolean = false,
    val allowBackNavigation: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class StudyConclusionDto(
    val id: String,
    val title: String,
    val summary: String,
    val reasoningSteps: List<String> = emptyList(),
    val verseReferences: List<VerseReferenceDto> = emptyList(),
    val reflectionQuestion: String = "",
    val relatedStudyIds: List<String> = emptyList()
)

@Serializable
data class VerseDuelDto(
    val id: String,
    val title: String,
    val passageA: VerseReferenceDto,
    val contextA: String,
    val passageB: VerseReferenceDto,
    val contextB: String,
    val analysisQuestion: String,
    val suggestedSynthesis: String = ""
)

@Serializable
data class InvestigationClueDto(
    val id: String,
    val testamentSection: String,
    val verseReference: VerseReferenceDto,
    val observationPrompt: String,
    val keyInsight: String
)

@Serializable
data class BibleInvestigationDto(
    val id: String,
    val centralQuestion: String,
    val clues: List<InvestigationClueDto> = emptyList(),
    val synthesisPrompt: String = ""
)

@Serializable
data class StudyDocumentDto(
    val schemaVersion: Int = 0,
    val studyId: String = "",
    val contentVersion: Int = 1,
    val locale: String = "fr",
    val title: String = "",
    val subtitle: String = "",
    val description: String = "",
    val categoryId: String = "",
    val tags: List<String> = emptyList(),
    val perspective: String = "SDA",
    val difficulty: String = "BEGINNER",
    val estimatedMinutes: Int = 10,
    val editorialStatus: String = "DEMO",
    val startNodeId: String = "",
    val prerequisiteStudyIds: List<String> = emptyList(),
    val metadata: StudyMetadataDto = StudyMetadataDto(),
    val nodes: List<DialogueNodeDto> = emptyList(),
    val conclusions: List<StudyConclusionDto> = emptyList(),
    val verseDuels: List<VerseDuelDto> = emptyList(),
    val investigation: BibleInvestigationDto? = null
)

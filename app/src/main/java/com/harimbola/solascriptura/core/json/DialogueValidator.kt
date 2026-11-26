package com.harimbola.solascriptura.core.json

import com.harimbola.solascriptura.core.common.AppConstants
import java.security.MessageDigest

sealed interface ValidationResult {
    data class Valid(val document: StudyDocumentDto, val checksum: String) : ValidationResult
    data class Invalid(val errors: List<String>) : ValidationResult
}

object DialogueValidator {

    fun parseAndValidate(rawJson: String): ValidationResult {
        if (rawJson.isBlank()) {
            return ValidationResult.Invalid(listOf("JSON content is empty."))
        }
        if (rawJson.toByteArray(Charsets.UTF_8).size > AppConstants.MAX_JSON_SIZE_BYTES) {
            return ValidationResult.Invalid(listOf("JSON exceeds maximum allowed size (${AppConstants.MAX_JSON_SIZE_BYTES} bytes)."))
        }

        val document = try {
            StudyJsonConfig.json.decodeFromString<StudyDocumentDto>(rawJson)
        } catch (e: Exception) {
            return ValidationResult.Invalid(listOf("Invalid JSON syntax: ${e.message ?: "parse error"}"))
        }

        val errors = validateDocument(document)
        return if (errors.isEmpty()) {
            ValidationResult.Valid(document, sha256(rawJson))
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    fun validateDocument(doc: StudyDocumentDto): List<String> {
        val errors = mutableListOf<String>()

        // Rule 2 & 3 & 15: studyId, schemaVersion, compatibility
        if (doc.studyId.isBlank()) {
            errors.add("Missing or blank studyId.")
        }
        if (doc.schemaVersion <= 0) {
            errors.add("Missing or invalid schemaVersion (${doc.schemaVersion}).")
        } else if (doc.schemaVersion > AppConstants.SUPPORTED_SCHEMA_VERSION) {
            errors.add("Unsupported schemaVersion ${doc.schemaVersion}. Max supported: ${AppConstants.SUPPORTED_SCHEMA_VERSION}.")
        }
        if (doc.title.isBlank()) {
            errors.add("Missing study title.")
        }
        if (doc.nodes.isEmpty()) {
            errors.add("Study contains no dialogue nodes.")
            return errors
        }

        // Rule 4: Unique IDs
        val nodeIds = mutableSetOf<String>()
        doc.nodes.forEach { node ->
            if (node.id.isBlank()) {
                errors.add("Encountered node with blank id.")
            } else if (!nodeIds.add(node.id)) {
                errors.add("Duplicate node id: ${node.id}")
            }
            val choiceIds = mutableSetOf<String>()
            node.choices.forEach { choice ->
                if (choice.id.isBlank()) {
                    errors.add("Node ${node.id} contains a choice with blank id.")
                } else if (!choiceIds.add(choice.id)) {
                    errors.add("Duplicate choice id '${choice.id}' in node ${node.id}.")
                }
            }
        }

        val conclusionIds = mutableSetOf<String>()
        doc.conclusions.forEach { conclusion ->
            if (conclusion.id.isBlank()) {
                errors.add("Encountered conclusion with blank id.")
            } else if (!conclusionIds.add(conclusion.id)) {
                errors.add("Duplicate conclusion id: ${conclusion.id}")
            }
            conclusion.verseReferences.forEachIndexed { index, ref ->
                validateVerseReference(ref, "Conclusion ${conclusion.id} ref #$index", errors)
            }
        }

        // Rule 5: startNodeId exists
        if (doc.startNodeId.isBlank() || doc.startNodeId !in nodeIds) {
            errors.add("startNodeId '${doc.startNodeId}' does not exist in nodes.")
        }

        val nodeMap = doc.nodes.associateBy { it.id }

        // Rules 6, 7, 8, 9, 10, 11
        doc.nodes.forEach { node ->
            if (node.phase == DialoguePhaseDto.UNKNOWN) {
                errors.add("Node ${node.id} has unknown/unsupported phase.")
            }
            if (node.type == DialogueNodeTypeDto.UNKNOWN) {
                errors.add("Node ${node.id} has unknown/unsupported type.")
            }
            node.verseReferences.forEachIndexed { idx, ref ->
                validateVerseReference(ref, "Node ${node.id} ref #$idx", errors)
            }
            node.conclusionId?.let { cId ->
                if (cId !in conclusionIds) {
                    errors.add("Node ${node.id} references unknown conclusionId '$cId'.")
                }
            }
            node.choices.forEach { choice ->
                choice.nextNodeId?.let { target ->
                    if (target !in nodeIds) {
                        errors.add("Node ${node.id} choice ${choice.id} targets missing node '$target'.")
                    }
                }
                choice.extraVerses.forEachIndexed { idx, ref ->
                    validateVerseReference(ref, "Node ${node.id} choice ${choice.id} extraVerse #$idx", errors)
                }
            }

            val isEndNode = node.terminal || node.type == DialogueNodeTypeDto.END || node.type == DialogueNodeTypeDto.SYNTHESIS
            val hasOutgoing = node.choices.any { it.nextNodeId != null }
            if (!isEndNode && !hasOutgoing && node.conclusionId == null) {
                errors.add("Non-terminal node ${node.id} has neither outgoing transitions nor a conclusionId.")
            }
        }

        // Rule 12: Reachability from startNodeId
        if (doc.startNodeId in nodeMap) {
            val reachable = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(doc.startNodeId)
            reachable.add(doc.startNodeId)
            while (queue.isNotEmpty()) {
                val currentId = queue.removeFirst()
                val currentNode = nodeMap[currentId] ?: continue
                currentNode.choices.forEach { choice ->
                    val next = choice.nextNodeId
                    if (next != null && next in nodeMap && reachable.add(next)) {
                        queue.add(next)
                    }
                }
            }
            val unreachable = nodeIds - reachable
            if (unreachable.isNotEmpty()) {
                errors.add("Unreachable nodes from startNodeId: ${unreachable.joinToString(", ")}")
            }

            // Rule 13 & 14: Verify at least one bounded path to a terminal/synthesis node exists (no inescapable cycle)
            val canReachTerminal = computeNodesReachingTerminal(doc.nodes)
            if (doc.startNodeId !in canReachTerminal) {
                errors.add("No valid path from startNodeId '${doc.startNodeId}' reaches a terminal or synthesis node (inescapable cycle).")
            }
            // Also verify shortest path from start to any terminal is within MAX_DIALOGUE_STEPS
            val minSteps = shortestPathToTerminal(doc.startNodeId, nodeMap)
            if (minSteps == null || minSteps > AppConstants.MAX_DIALOGUE_STEPS) {
                errors.add("Minimum path length exceeds step bound (${AppConstants.MAX_DIALOGUE_STEPS}).")
            }
        }

        return errors
    }

    private fun validateVerseReference(
        ref: VerseReferenceDto,
        location: String,
        errors: MutableList<String>
    ) {
        if (ref.bookId.isBlank() || ref.bookName.isBlank()) {
            errors.add("$location has blank bookId or bookName.")
        }
        if (ref.chapter <= 0) {
            errors.add("$location has invalid chapter ${ref.chapter}.")
        }
        if (ref.startVerse <= 0) {
            errors.add("$location has invalid startVerse ${ref.startVerse}.")
        }
        if (ref.endVerse < ref.startVerse) {
            errors.add("$location has endVerse (${ref.endVerse}) < startVerse (${ref.startVerse}).")
        }
    }

    private fun computeNodesReachingTerminal(nodes: List<DialogueNodeDto>): Set<String> {
        val reverseAdj = mutableMapOf<String, MutableList<String>>()
        val canReach = mutableSetOf<String>()
        val queue = ArrayDeque<String>()

        nodes.forEach { node ->
            val isTerminal = node.terminal ||
                node.type == DialogueNodeTypeDto.END ||
                node.type == DialogueNodeTypeDto.SYNTHESIS ||
                node.conclusionId != null ||
                node.choices.any { it.nextNodeId == null }
            if (isTerminal) {
                canReach.add(node.id)
                queue.add(node.id)
            }
            node.choices.forEach { choice ->
                val target = choice.nextNodeId
                if (target != null) {
                    reverseAdj.getOrPut(target) { mutableListOf() }.add(node.id)
                }
            }
        }

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            reverseAdj[current]?.forEach { parent ->
                if (canReach.add(parent)) {
                    queue.add(parent)
                }
            }
        }
        return canReach
    }

    private fun shortestPathToTerminal(
        startId: String,
        nodeMap: Map<String, DialogueNodeDto>
    ): Int? {
        val visited = mutableSetOf(startId)
        val queue = ArrayDeque<Pair<String, Int>>()
        queue.add(startId to 0)
        while (queue.isNotEmpty()) {
            val (id, dist) = queue.removeFirst()
            val node = nodeMap[id] ?: continue
            val isTerminal = node.terminal ||
                node.type == DialogueNodeTypeDto.END ||
                node.type == DialogueNodeTypeDto.SYNTHESIS ||
                node.conclusionId != null ||
                node.choices.any { it.nextNodeId == null }
            if (isTerminal) return dist
            node.choices.forEach { choice ->
                val next = choice.nextNodeId
                if (next != null && visited.add(next)) {
                    queue.add(next to dist + 1)
                }
            }
        }
        return null
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

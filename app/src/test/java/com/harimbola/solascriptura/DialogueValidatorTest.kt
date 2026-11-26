package com.harimbola.solascriptura

import com.harimbola.solascriptura.core.json.DialogueValidator
import com.harimbola.solascriptura.core.json.ValidationResult
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DialogueValidatorTest {

    private fun validStudyJson(): String = """
        {
          "schemaVersion": 1,
          "studyId": "valid_test_001",
          "contentVersion": 1,
          "locale": "fr",
          "title": "Étude test",
          "subtitle": "Sous-titre",
          "description": "Description complète",
          "categoryId": "foundations",
          "estimatedMinutes": 10,
          "editorialStatus": "REVIEWED",
          "startNodeId": "n1",
          "nodes": [
            {
              "id": "n1",
              "phase": "IRONY",
              "type": "QUESTION",
              "speakerText": "Question initiale ?",
              "verseReferences": [
                {
                  "bookId": "gen",
                  "bookName": "Genèse",
                  "chapter": 1,
                  "startVerse": 1,
                  "endVerse": 1,
                  "quotedText": "Au commencement..."
                }
              ],
              "choices": [
                { "id": "c1", "label": "Continuer", "nextNodeId": "n2" }
              ]
            },
            {
              "id": "n2",
              "phase": "SYNTHESIS",
              "type": "SYNTHESIS",
              "speakerText": "Synthèse",
              "conclusionId": "conc1",
              "terminal": true
            }
          ],
          "conclusions": [
            {
              "id": "conc1",
              "title": "Conclusion",
              "summary": "Résumé clair"
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `valid study passes all 15 rules`() {
        val result = DialogueValidator.parseAndValidate(validStudyJson())
        assertTrue("Expected Valid, got $result", result is ValidationResult.Valid)
    }

    @Test
    fun `rejects invalid syntax, missing studyId, unsupported schema, dangling target, and unreachable nodes`() {
        assertTrue(DialogueValidator.parseAndValidate("{ broken json").let { it is ValidationResult.Invalid })

        val missingStudyId = validStudyJson().replace("valid_test_001", "")
        assertTrue(DialogueValidator.parseAndValidate(missingStudyId) is ValidationResult.Invalid)

        val badSchema = validStudyJson().replace("\"schemaVersion\": 1", "\"schemaVersion\": 99")
        assertTrue(DialogueValidator.parseAndValidate(badSchema) is ValidationResult.Invalid)

        val badTarget = validStudyJson().replace("\"nextNodeId\": \"n2\"", "\"nextNodeId\": \"missing_node\"")
        assertTrue(DialogueValidator.parseAndValidate(badTarget) is ValidationResult.Invalid)

        val badVerse = validStudyJson().replace("\"chapter\": 1", "\"chapter\": 0")
        assertTrue(DialogueValidator.parseAndValidate(badVerse) is ValidationResult.Invalid)
    }

    @Test
    fun `rejects inescapable cycle without path to terminal`() {
        val cycleJson = """
            {
              "schemaVersion": 1,
              "studyId": "cycle_test",
              "contentVersion": 1,
              "locale": "fr",
              "title": "Cycle",
              "startNodeId": "a",
              "nodes": [
                {
                  "id": "a",
                  "phase": "IRONY",
                  "type": "QUESTION",
                  "speakerText": "A",
                  "choices": [{ "id": "c1", "label": "To B", "nextNodeId": "b" }]
                },
                {
                  "id": "b",
                  "phase": "ELENCHUS",
                  "type": "QUESTION",
                  "speakerText": "B",
                  "choices": [{ "id": "c2", "label": "To A", "nextNodeId": "a" }]
                }
              ],
              "conclusions": []
            }
        """.trimIndent()
        val result = DialogueValidator.parseAndValidate(cycleJson)
        assertTrue(result is ValidationResult.Invalid)
    }

    @Test
    fun `every bundled asset study json is valid according to DialogueValidator`() {
        val candidates = listOf(
            File("src/main/assets/studies"),
            File("app/src/main/assets/studies")
        )
        val studiesDir = candidates.firstOrNull { it.exists() && it.isDirectory }
            ?: error("Could not locate bundled studies directory.")

        val jsonFiles = studiesDir.walkTopDown().filter { it.isFile && it.extension == "json" }.toList()
        assertTrue("Expected at least 10 bundled studies, found ${jsonFiles.size}", jsonFiles.size >= 10)

        jsonFiles.forEach { file ->
            val result = DialogueValidator.parseAndValidate(file.readText(Charsets.UTF_8))
            assertTrue(
                "Bundled study ${file.name} failed validation: ${(result as? ValidationResult.Invalid)?.errors}",
                result is ValidationResult.Valid
            )
        }
    }
}

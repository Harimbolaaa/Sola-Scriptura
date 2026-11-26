package com.harimbola.solascriptura.domain.usecase

import com.harimbola.solascriptura.core.engine.EngineVerseReference
import com.harimbola.solascriptura.domain.model.DailyChallenge
import com.harimbola.solascriptura.domain.model.Study
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.abs

class CalculateDailyChallengeUseCase @Inject constructor() {

    operator fun invoke(
        studies: List<Study>,
        completedDateIso: String?,
        locale: String,
        yearDaySeed: Int? = null,
        dateIsoOverride: String? = null
    ): DailyChallenge? {
        if (studies.isEmpty()) return null

        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)
        val dayOfYear = yearDaySeed ?: cal.get(Calendar.DAY_OF_YEAR)
        val month = cal.get(Calendar.MONTH) + 1
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dateIso = dateIsoOverride ?: "%04d-%02d-%02d".format(year, month, day)

        val sorted = studies.sortedBy { it.studyId }
        val deterministicIndex = abs((year * 367 + dayOfYear)) % sorted.size
        val selectedStudy = sorted[deterministicIndex]

        val allVerses = selectedStudy.graph.nodes.values
            .flatMap { it.verseReferences }
            .distinctBy { "${it.bookId}:${it.chapter}:${it.startVerse}-${it.endVerse}" }
            .take(3)

        val prompt = buildPrompt(selectedStudy, allVerses, locale)

        return DailyChallenge(
            dateIso = dateIso,
            studyId = selectedStudy.studyId,
            studyTitle = selectedStudy.title,
            promptText = prompt,
            verses = allVerses,
            isCompletedToday = completedDateIso == dateIso
        )
    }

    private fun buildPrompt(
        study: Study,
        verses: List<EngineVerseReference>,
        locale: String
    ): String {
        val count = verses.size.coerceAtLeast(1)
        return when (locale) {
            "en" -> "Examine $count passage(s) today from \"${study.title}\" and note one question it raises."
            "mg" -> "Diniho anio ireo andininy $count avy amin'ny \"${study.title}\" ary soraty ny fanontaniana iray mipetraka."
            else -> "Examine aujourd'hui $count passage(s) de l'étude « ${study.title} » et note une question de réflexion."
        }
    }
}

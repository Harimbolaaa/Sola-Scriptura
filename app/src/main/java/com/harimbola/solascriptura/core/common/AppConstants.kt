package com.harimbola.solascriptura.core.common

object AppConstants {
    const val SUPPORTED_SCHEMA_VERSION = 1
    const val MAX_DIALOGUE_STEPS = 60
    const val MAX_JSON_SIZE_BYTES = 2 * 1024 * 1024 // 2 MB safety limit
    const val DATABASE_NAME = "sola_scriptura.db"
    const val PREFERENCES_NAME = "sola_scriptura_prefs"

    const val ASSET_STUDIES_ROOT = "studies"
    val STUDY_CATEGORIES_ORDER = listOf(
        "foundations",
        "salvation",
        "forgotten_truths",
        "prophecies"
    )

    const val SUPPORTED_LOCALES_FR = "fr"
    const val SUPPORTED_LOCALES_MG = "mg"
    const val SUPPORTED_LOCALES_EN = "en"
    val SUPPORTED_LOCALES = listOf(
        SUPPORTED_LOCALES_FR,
        SUPPORTED_LOCALES_MG,
        SUPPORTED_LOCALES_EN
    )

    const val SYNC_WORK_NAME = "sola_scriptura_sync_work"
    const val PERIODIC_SYNC_WORK_NAME = "sola_scriptura_periodic_sync"
}

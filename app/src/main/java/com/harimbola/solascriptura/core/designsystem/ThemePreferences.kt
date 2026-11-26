package com.harimbola.solascriptura.core.designsystem

enum class AppVisualTheme(val displayNameFr: String, val displayNameEn: String, val displayNameMg: String) {
    CLASSIC_PAPER("Classic Paper", "Classic Paper", "Taratasy Klasika"),
    MIDNIGHT_STUDY("Midnight Study", "Midnight Study", "Fianarana Alina"),
    SAGE_NATURE("Sage Nature", "Sage Nature", "Zavaboary Maitso"),
    WARM_SAND("Warm Sand", "Warm Sand", "Fasika Mafana"),
    MINIMAL_CLOUD("Minimal Cloud", "Minimal Cloud", "Rahona Tsotra"),
    HIGH_CONTRAST("High Contrast", "High Contrast", "Fifanarahana Avo");

    fun label(locale: String): String = when (locale) {
        "en" -> displayNameEn
        "mg" -> displayNameMg
        else -> displayNameFr
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

enum class AccentPreset(val labelFr: String, val labelEn: String, val labelMg: String) {
    ANTIQUE_GOLD("Or antique", "Antique Gold", "Volamena"),
    SAGE_GREEN("Sauge", "Sage", "Maitso malefaka"),
    TERRACOTTA("Terracotta", "Terracotta", "Tanimanga"),
    COBALT_INK("Encre cobalt", "Cobalt Ink", "Manga lalina")
}

enum class FontStylePreset(val labelFr: String, val labelEn: String, val labelMg: String) {
    CLASSIC_SERIF("Sérif classique (Crimson Pro)", "Classic Serif", "Soratra Klasika"),
    EDITORIAL_SERIF("Éditorial (Source Serif)", "Editorial Serif", "Soratra Gazety"),
    MODERN_SANS("Moderne (Plus Jakarta)", "Modern Sans", "Soratra Maoderina"),
    STUDY_MONO("Étude structurée (JetBrains)", "Study Mono", "Soratra Fandinihana")
}

enum class CardDensity(val labelFr: String, val labelEn: String, val labelMg: String) {
    COMPACT("Compacte", "Compact", "Fohy"),
    COMFORTABLE("Confortable", "Comfortable", "Mahazo aina"),
    SPACIOUS("Aérée", "Spacious", "Malalaka")
}

enum class SyncMode(val labelFr: String, val labelEn: String, val labelMg: String) {
    AUTO("Automatique", "Automatic", "Mandeha ho azy"),
    WIFI_ONLY("Wi-Fi uniquement", "Wi-Fi only", "Wi-Fi ihany"),
    MANUAL("Manuelle", "Manual", "Tànana")
}

data class ThemePreferences(
    val visualTheme: AppVisualTheme = AppVisualTheme.CLASSIC_PAPER,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val useDynamicColors: Boolean = false,
    val accentPreset: AccentPreset = AccentPreset.ANTIQUE_GOLD,
    val fontStyle: FontStylePreset = FontStylePreset.CLASSIC_SERIF,
    val fontScale: Float = 1.0f,
    val lineSpacingMultiplier: Float = 1.15f,
    val cardDensity: CardDensity = CardDensity.COMFORTABLE,
    val animationsEnabled: Boolean = true
)

data class StudyPreferences(
    val language: String = "fr",
    val preferredDurationMinutes: Int = 15,
    val autoResumeLastStudy: Boolean = true,
    val dailyNotificationsEnabled: Boolean = false,
    val showVerseReferences: Boolean = true,
    val showBiblicalContext: Boolean = true
)

data class SyncPreferences(
    val syncMode: SyncMode = SyncMode.WIFI_ONLY,
    val syncSettingsEnabled: Boolean = false,
    val lastSyncTimestamp: Long = 0L
)

data class UserPreferences(
    val onboardingCompleted: Boolean = false,
    val theme: ThemePreferences = ThemePreferences(),
    val study: StudyPreferences = StudyPreferences(),
    val sync: SyncPreferences = SyncPreferences(),
    val accountUserId: String? = null,
    val accountEmail: String? = null,
    val accountName: String? = null,
    val dailyChallengeCompletedDate: String? = null
)

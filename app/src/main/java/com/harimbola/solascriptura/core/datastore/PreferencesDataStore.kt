package com.harimbola.solascriptura.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harimbola.solascriptura.core.common.AppConstants
import com.harimbola.solascriptura.core.designsystem.AccentPreset
import com.harimbola.solascriptura.core.designsystem.AppVisualTheme
import com.harimbola.solascriptura.core.designsystem.CardDensity
import com.harimbola.solascriptura.core.designsystem.FontStylePreset
import com.harimbola.solascriptura.core.designsystem.StudyPreferences
import com.harimbola.solascriptura.core.designsystem.SyncMode
import com.harimbola.solascriptura.core.designsystem.SyncPreferences
import com.harimbola.solascriptura.core.designsystem.ThemeMode
import com.harimbola.solascriptura.core.designsystem.ThemePreferences
import com.harimbola.solascriptura.core.designsystem.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.solaDataStore: DataStore<Preferences> by preferencesDataStore(
    name = AppConstants.PREFERENCES_NAME
)

class PreferencesDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val VISUAL_THEME = stringPreferencesKey("visual_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USE_DYNAMIC_COLORS = booleanPreferencesKey("use_dynamic_colors")
        val ACCENT_PRESET = stringPreferencesKey("accent_preset")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val LINE_SPACING = floatPreferencesKey("line_spacing")
        val CARD_DENSITY = stringPreferencesKey("card_density")
        val ANIMATIONS_ENABLED = booleanPreferencesKey("animations_enabled")

        val LANGUAGE = stringPreferencesKey("study_language")
        val PREFERRED_DURATION = intPreferencesKey("preferred_duration")
        val AUTO_RESUME = booleanPreferencesKey("auto_resume")
        val DAILY_NOTIFICATIONS = booleanPreferencesKey("daily_notifications")
        val SHOW_VERSE_REFERENCES = booleanPreferencesKey("show_verse_references")
        val SHOW_BIBLICAL_CONTEXT = booleanPreferencesKey("show_biblical_context")

        val SYNC_MODE = stringPreferencesKey("sync_mode")
        val SYNC_SETTINGS_ENABLED = booleanPreferencesKey("sync_settings_enabled")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")

        val ACCOUNT_USER_ID = stringPreferencesKey("account_user_id")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val ACCOUNT_NAME = stringPreferencesKey("account_name")
        val DAILY_CHALLENGE_COMPLETED_DATE = stringPreferencesKey("daily_challenge_completed_date")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            theme = ThemePreferences(
                visualTheme = enumValueOrDefault(prefs[Keys.VISUAL_THEME], AppVisualTheme.CLASSIC_PAPER),
                themeMode = enumValueOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM),
                useDynamicColors = prefs[Keys.USE_DYNAMIC_COLORS] ?: false,
                accentPreset = enumValueOrDefault(prefs[Keys.ACCENT_PRESET], AccentPreset.ANTIQUE_GOLD),
                fontStyle = enumValueOrDefault(prefs[Keys.FONT_STYLE], FontStylePreset.CLASSIC_SERIF),
                fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
                lineSpacingMultiplier = prefs[Keys.LINE_SPACING] ?: 1.15f,
                cardDensity = enumValueOrDefault(prefs[Keys.CARD_DENSITY], CardDensity.COMFORTABLE),
                animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true
            ),
            study = StudyPreferences(
                language = prefs[Keys.LANGUAGE] ?: AppConstants.SUPPORTED_LOCALES_FR,
                preferredDurationMinutes = prefs[Keys.PREFERRED_DURATION] ?: 15,
                autoResumeLastStudy = prefs[Keys.AUTO_RESUME] ?: true,
                dailyNotificationsEnabled = prefs[Keys.DAILY_NOTIFICATIONS] ?: false,
                showVerseReferences = prefs[Keys.SHOW_VERSE_REFERENCES] ?: true,
                showBiblicalContext = prefs[Keys.SHOW_BIBLICAL_CONTEXT] ?: true
            ),
            sync = SyncPreferences(
                syncMode = enumValueOrDefault(prefs[Keys.SYNC_MODE], SyncMode.WIFI_ONLY),
                syncSettingsEnabled = prefs[Keys.SYNC_SETTINGS_ENABLED] ?: false,
                lastSyncTimestamp = prefs[Keys.LAST_SYNC_TIMESTAMP] ?: 0L
            ),
            accountUserId = prefs[Keys.ACCOUNT_USER_ID],
            accountEmail = prefs[Keys.ACCOUNT_EMAIL],
            accountName = prefs[Keys.ACCOUNT_NAME],
            dailyChallengeCompletedDate = prefs[Keys.DAILY_CHALLENGE_COMPLETED_DATE]
        )
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun updateThemePreferences(transform: (ThemePreferences) -> ThemePreferences) {
        dataStore.edit { prefs ->
            val current = ThemePreferences(
                visualTheme = enumValueOrDefault(prefs[Keys.VISUAL_THEME], AppVisualTheme.CLASSIC_PAPER),
                themeMode = enumValueOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM),
                useDynamicColors = prefs[Keys.USE_DYNAMIC_COLORS] ?: false,
                accentPreset = enumValueOrDefault(prefs[Keys.ACCENT_PRESET], AccentPreset.ANTIQUE_GOLD),
                fontStyle = enumValueOrDefault(prefs[Keys.FONT_STYLE], FontStylePreset.CLASSIC_SERIF),
                fontScale = prefs[Keys.FONT_SCALE] ?: 1.0f,
                lineSpacingMultiplier = prefs[Keys.LINE_SPACING] ?: 1.15f,
                cardDensity = enumValueOrDefault(prefs[Keys.CARD_DENSITY], CardDensity.COMFORTABLE),
                animationsEnabled = prefs[Keys.ANIMATIONS_ENABLED] ?: true
            )
            val updated = transform(current)
            prefs[Keys.VISUAL_THEME] = updated.visualTheme.name
            prefs[Keys.THEME_MODE] = updated.themeMode.name
            prefs[Keys.USE_DYNAMIC_COLORS] = updated.useDynamicColors
            prefs[Keys.ACCENT_PRESET] = updated.accentPreset.name
            prefs[Keys.FONT_STYLE] = updated.fontStyle.name
            prefs[Keys.FONT_SCALE] = updated.fontScale
            prefs[Keys.LINE_SPACING] = updated.lineSpacingMultiplier
            prefs[Keys.CARD_DENSITY] = updated.cardDensity.name
            prefs[Keys.ANIMATIONS_ENABLED] = updated.animationsEnabled
        }
    }

    suspend fun updateStudyPreferences(transform: (StudyPreferences) -> StudyPreferences) {
        dataStore.edit { prefs ->
            val current = StudyPreferences(
                language = prefs[Keys.LANGUAGE] ?: AppConstants.SUPPORTED_LOCALES_FR,
                preferredDurationMinutes = prefs[Keys.PREFERRED_DURATION] ?: 15,
                autoResumeLastStudy = prefs[Keys.AUTO_RESUME] ?: true,
                dailyNotificationsEnabled = prefs[Keys.DAILY_NOTIFICATIONS] ?: false,
                showVerseReferences = prefs[Keys.SHOW_VERSE_REFERENCES] ?: true,
                showBiblicalContext = prefs[Keys.SHOW_BIBLICAL_CONTEXT] ?: true
            )
            val updated = transform(current)
            prefs[Keys.LANGUAGE] = updated.language
            prefs[Keys.PREFERRED_DURATION] = updated.preferredDurationMinutes
            prefs[Keys.AUTO_RESUME] = updated.autoResumeLastStudy
            prefs[Keys.DAILY_NOTIFICATIONS] = updated.dailyNotificationsEnabled
            prefs[Keys.SHOW_VERSE_REFERENCES] = updated.showVerseReferences
            prefs[Keys.SHOW_BIBLICAL_CONTEXT] = updated.showBiblicalContext
        }
    }

    suspend fun updateSyncPreferences(transform: (SyncPreferences) -> SyncPreferences) {
        dataStore.edit { prefs ->
            val current = SyncPreferences(
                syncMode = enumValueOrDefault(prefs[Keys.SYNC_MODE], SyncMode.WIFI_ONLY),
                syncSettingsEnabled = prefs[Keys.SYNC_SETTINGS_ENABLED] ?: false,
                lastSyncTimestamp = prefs[Keys.LAST_SYNC_TIMESTAMP] ?: 0L
            )
            val updated = transform(current)
            prefs[Keys.SYNC_MODE] = updated.syncMode.name
            prefs[Keys.SYNC_SETTINGS_ENABLED] = updated.syncSettingsEnabled
            prefs[Keys.LAST_SYNC_TIMESTAMP] = updated.lastSyncTimestamp
        }
    }

    suspend fun setAccountInfo(userId: String?, email: String?, name: String?) {
        dataStore.edit { prefs ->
            if (userId != null) prefs[Keys.ACCOUNT_USER_ID] = userId else prefs.remove(Keys.ACCOUNT_USER_ID)
            if (email != null) prefs[Keys.ACCOUNT_EMAIL] = email else prefs.remove(Keys.ACCOUNT_EMAIL)
            if (name != null) prefs[Keys.ACCOUNT_NAME] = name else prefs.remove(Keys.ACCOUNT_NAME)
        }
    }

    suspend fun setDailyChallengeCompletedDate(dateIso: String) {
        dataStore.edit { it[Keys.DAILY_CHALLENGE_COMPLETED_DATE] = dateIso }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, default: T): T {
        if (value == null) return default
        return enumValues<T>().firstOrNull { it.name == value } ?: default
    }
}

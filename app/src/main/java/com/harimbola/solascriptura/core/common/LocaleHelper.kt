package com.harimbola.solascriptura.core.common

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun localizedContext(base: Context, languageCode: String): Context {
        val normalized = when (languageCode) {
            AppConstants.SUPPORTED_LOCALES_MG -> "mg"
            AppConstants.SUPPORTED_LOCALES_EN -> "en"
            else -> "fr"
        }
        val locale = Locale.Builder().setLanguage(normalized).build()
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}

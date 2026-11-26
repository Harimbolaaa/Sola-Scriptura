package com.example

import android.content.ContextWrapper
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.harimbola.solascriptura.app.AppViewModel
import com.harimbola.solascriptura.app.SolaScripturaNavHost
import com.harimbola.solascriptura.core.common.LocaleHelper
import com.harimbola.solascriptura.core.designsystem.SolaScripturaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by appViewModel.userPreferences.collectAsStateWithLifecycle()
            val languageCode = prefs.study.language
            val localizedContext = remember(languageCode) {
                LocaleHelper.localizedContext(this@MainActivity, languageCode)
            }
            val wrappedActivityContext = remember(localizedContext) {
                object : ContextWrapper(this@MainActivity) {
                    override fun getResources(): Resources = localizedContext.resources
                }
            }

            CompositionLocalProvider(
                LocalContext provides wrappedActivityContext,
                LocalConfiguration provides localizedContext.resources.configuration
            ) {
                SolaScripturaTheme(preferences = prefs.theme) {
                    SolaScripturaNavHost(onboardingCompleted = prefs.onboardingCompleted)
                }
            }
        }
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.harimbola.solascriptura.core.common.LocaleHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class ExampleRobolectricTest {

    @Test
    fun `read localized app strings from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val frContext = LocaleHelper.localizedContext(context, "fr")
        assertEquals("Sola Scriptura", frContext.getString(R.string.app_name))

        val enContext = LocaleHelper.localizedContext(context, "en")
        assertEquals("Studies", enContext.getString(R.string.nav_studies))

        val mgContext = LocaleHelper.localizedContext(context, "mg")
        assertEquals("Fianarana", mgContext.getString(R.string.nav_studies))
    }
}

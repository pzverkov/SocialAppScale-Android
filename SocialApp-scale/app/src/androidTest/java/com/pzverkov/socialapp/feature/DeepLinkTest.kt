package com.pzverkov.socialapp.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.pzverkov.socialapp.MainActivity
import com.pzverkov.socialapp.awaitText
import com.pzverkov.socialapp.resetAppState
import org.junit.Rule
import org.junit.Test

/**
 * Launches the activity directly with the deep-link intent (empty compose rule + ActivityScenario)
 * rather than mutating intent + recreate(): recreate() leaves createAndroidComposeRule bound to the
 * destroyed activity, and the detail screen's indeterminate progress indicator keeps the recreate
 * synchronization from ever settling.
 */
class DeepLinkTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private fun launchDeepLink(uri: String): ActivityScenario<MainActivity> {
        resetAppState()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(uri)
        }
        return ActivityScenario.launch(intent)
    }

    @Test
    fun customSchemeDeeplink_opensDetailScreen() {
        launchDeepLink("socialapp://item/1").use {
            composeRule.awaitText("Vintage Camera")
            composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        }
    }

    @Test
    fun deeplink_withRefParam_opensDetailScreen() {
        launchDeepLink("socialapp://item/2?ref=abc12345").use {
            composeRule.awaitText("Mountain Bike")
            composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
        }
    }
}

package com.pzverkov.socialapp.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.pzverkov.socialapp.MainActivity
import org.junit.Rule
import org.junit.Test

class DeepLinkTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun customSchemeDeeplink_opensDetailScreen() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("socialapp://item/1"))
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.intent = intent
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
    }

    @Test
    fun deeplink_withRefParam_opensDetailScreen() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("socialapp://item/2?ref=abc12345"))
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.intent = intent
        }
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
    }
}

package com.pzverkov.socialapp.feature

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.pzverkov.socialapp.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class DeepLinkTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

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

package com.pzverkov.socialapp.feature.itemlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.pzverkov.socialapp.FakeItemRepositoryImpl
import com.pzverkov.socialapp.MainActivity
import com.pzverkov.socialapp.TestAppGraph
import com.pzverkov.socialapp.TestApplication
import com.pzverkov.socialapp.awaitText
import com.pzverkov.socialapp.core.domain.NetworkResult
import com.pzverkov.socialapp.core.model.ErrorType
import com.pzverkov.socialapp.resetAppState
import org.junit.Rule
import org.junit.Test

/**
 * The error path needs the repository to fail BEFORE the screen loads, so it presets the fake and
 * launches the activity itself with an empty compose rule rather than recreating an already-launched
 * one - createAndroidComposeRule stays bound to the original activity across recreate().
 */
class ItemListErrorStateTest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Test
    fun errorState_showsErrorAndRetryButton() {
        resetAppState()
        val graph = ApplicationProvider.getApplicationContext<TestApplication>().graph as TestAppGraph
        (graph.itemRepository as FakeItemRepositoryImpl).itemsResult =
            NetworkResult.Error(ErrorType.NETWORK)

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.awaitText("Something went wrong")
            composeRule.onNodeWithText("Something went wrong").assertIsDisplayed()
            composeRule.onNodeWithText("Retry").assertIsDisplayed()
        }
    }
}

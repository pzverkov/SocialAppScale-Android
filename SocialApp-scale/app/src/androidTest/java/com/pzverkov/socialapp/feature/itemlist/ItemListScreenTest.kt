package com.pzverkov.socialapp.feature.itemlist

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pzverkov.socialapp.MainActivity
import com.pzverkov.socialapp.ResetAppStateRule
import com.pzverkov.socialapp.awaitText
import org.junit.Rule
import org.junit.Test

class ItemListScreenTest {

    // Reset shared singleton/Room state before the activity launches (outer rule runs first).
    @get:Rule(order = 0)
    val resetRule = ResetAppStateRule()

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun loadedState_showsItems() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
        composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
        // Two of the three items are located in New York (Vintage Camera, iPhone 13 Pro).
        composeRule.onAllNodesWithText("New York").assertCountEquals(2)
    }

    @Test
    fun loadedState_showsSearchBar() {
        composeRule.onNodeWithText("Search SocialApp\u2026").assertIsDisplayed()
    }

    @Test
    fun search_filtersItemsByTitle() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("Camera")
        // The query is debounced 300ms on the coroutine dispatcher; poll until it takes effect.
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Mountain Bike").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        composeRule.onNodeWithText("Mountain Bike").assertDoesNotExist()
    }

    @Test
    fun search_filtersItemsByLocation() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("London")
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Vintage Camera").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
        composeRule.onNodeWithText("Vintage Camera").assertDoesNotExist()
    }

    @Test
    fun search_noResults_showsSearchEmptyState() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("xyznonexistent")
        composeRule.awaitText("No results for \"xyznonexistent\"")
        composeRule.onNodeWithText("No results for \"xyznonexistent\"").assertIsDisplayed()
    }

    @Test
    fun favoriteToggle_changesIcon() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onAllNodes(
            hasContentDescription("Add to favorites"),
            useUnmergedTree = true,
        )[0].performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun itemClick_navigatesToDetail() {
        composeRule.awaitText("Vintage Camera")
        composeRule.onNodeWithText("Vintage Camera").performClick()
        composeRule.awaitText("Buy Now")
        composeRule.onNodeWithText("Buy Now").assertIsDisplayed()
    }
}

private fun hasContentDescription(value: String) =
    androidx.compose.ui.test.hasContentDescription(value)

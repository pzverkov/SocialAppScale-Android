package com.pzverkov.socialapp.feature.itemlist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.pzverkov.socialapp.FakeItemRepositoryImpl
import com.pzverkov.socialapp.MainActivity
import com.pzverkov.socialapp.core.network.ErrorType
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ItemListScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: ItemRepository

    private val fakeRepository: FakeItemRepositoryImpl
        get() = repository as FakeItemRepositoryImpl

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loadedState_showsItems() {
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
        composeRule.onNodeWithText("New York").assertIsDisplayed()
        composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
    }

    @Test
    fun loadedState_showsSearchBar() {
        composeRule.onNodeWithText("Search SocialApp\u2026").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorAndRetryButton() {
        fakeRepository.itemsResult = NetworkResult.Error(ErrorType.NETWORK)
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Something went wrong").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    @Test
    fun search_filtersItemsByTitle() {
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("Camera")
        waitForDebounce()
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        composeRule.onNodeWithText("Mountain Bike").assertDoesNotExist()
    }

    @Test
    fun search_filtersItemsByLocation() {
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("London")
        waitForDebounce()
        composeRule.onNodeWithText("Mountain Bike").assertIsDisplayed()
        composeRule.onNodeWithText("Vintage Camera").assertDoesNotExist()
    }

    @Test
    fun search_noResults_showsSearchEmptyState() {
        composeRule.onNodeWithText("Search SocialApp\u2026").performClick()
        composeRule.onNodeWithText("Search SocialApp\u2026").performTextInput("xyznonexistent")
        waitForDebounce()
        composeRule.onNodeWithText("No results for \"xyznonexistent\"").assertIsDisplayed()
    }

    @Test
    fun favoriteToggle_changesIcon() {
        composeRule.onAllNodes(
            hasContentDescription("Add to favorites"),
            useUnmergedTree = true,
        )[0].performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun itemClick_navigatesToDetail() {
        composeRule.onNodeWithText("Vintage Camera").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Buy Now").assertIsDisplayed()
    }

    // Advance the Compose clock past the 300ms debounce instead of Thread.sleep
    private fun waitForDebounce() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(400)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
    }
}

private fun hasContentDescription(value: String) =
    androidx.compose.ui.test.hasContentDescription(value)

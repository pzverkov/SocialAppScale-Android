package com.pzverkov.socialapp.feature.itemdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.pzverkov.socialapp.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ItemDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        navigateToDetail()
    }

    private fun navigateToDetail() {
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Vintage Camera").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun detailScreen_showsItemInfo() {
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
        composeRule.onNodeWithText("$150.00").assertIsDisplayed()
        composeRule.onNodeWithText("New York").assertIsDisplayed()
        composeRule.onNodeWithText("A beautiful vintage camera in excellent condition").assertIsDisplayed()
    }

    @Test
    fun detailScreen_showsSellerInfo() {
        composeRule.onNodeWithText("SocialUser1").assertIsDisplayed()
        composeRule.onNodeWithText("View profile").assertIsDisplayed()
    }

    @Test
    fun detailScreen_showsBuyButton() {
        composeRule.onNodeWithText("Buy Now").assertIsDisplayed()
    }

    @Test
    fun detailScreen_buyButton_showsSnackbar() {
        composeRule.onNodeWithText("Buy Now").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Purchase flow for Vintage Camera coming soon!").assertIsDisplayed()
    }

    @Test
    fun detailScreen_favoriteToggle() {
        composeRule.onNodeWithContentDescription("Add to favorites").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("Remove from favorites").assertIsDisplayed()
    }

    @Test
    fun detailScreen_backButton_returnsToList() {
        composeRule.onNodeWithContentDescription("Back").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Vintage Camera").assertIsDisplayed()
    }
}

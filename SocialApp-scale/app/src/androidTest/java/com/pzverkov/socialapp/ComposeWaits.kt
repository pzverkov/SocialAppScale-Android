package com.pzverkov.socialapp

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText

/**
 * Polls in real wall-clock time until a node with [text] exists. The list screen reaches its
 * Loaded state through a flow that debounces the search query by 300ms, so content appears shortly
 * after launch rather than synchronously; waitForIdle() does not wait for a coroutine delay (and
 * cannot settle while an indeterminate progress indicator animates), so tests must poll for the
 * content itself.
 */
fun ComposeTestRule.awaitText(text: String, timeoutMs: Long = 5_000) {
    waitUntil(timeoutMs) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

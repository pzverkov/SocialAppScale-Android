package com.pzverkov.socialapp.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.pzverkov.socialapp.core.model.ErrorType
import com.pzverkov.socialapp.core.ui.components.EmptyState
import com.pzverkov.socialapp.core.ui.components.ErrorState
import com.pzverkov.socialapp.core.ui.components.LoadingIndicator
import com.pzverkov.socialapp.core.ui.theme.SocialAppTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the shared design system, rendered by Roborazzi under Robolectric (JVM,
 * no emulator) at SDK 36 with native graphics. Goldens live in src/test/screenshots and are
 * checked in; `recordRoborazziDebug` regenerates them, `verifyRoborazziDebug` compares.
 *
 * Uses Roborazzi's content-lambda capture (no Activity), which Robolectric can render directly.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ComponentScreenshotTest {

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            SocialAppTheme {
                Box(
                    modifier = Modifier
                        .size(360.dp, 640.dp)
                        .background(MaterialTheme.colorScheme.background),
                ) {
                    content()
                }
            }
        }
    }

    @Test
    fun emptyState() = snapshot("empty_state") {
        EmptyState(
            title = "No items yet",
            subtitle = "Pull to refresh or check back later",
            modifier = Modifier.fillMaxSize(),
        )
    }

    @Test
    fun errorNetwork() = snapshot("error_network") {
        ErrorState(errorType = ErrorType.NETWORK, onRetry = {}, modifier = Modifier.fillMaxSize())
    }

    @Test
    fun errorUnknown() = snapshot("error_unknown") {
        ErrorState(errorType = ErrorType.UNKNOWN, onRetry = {}, modifier = Modifier.fillMaxSize())
    }

    @Test
    fun loading() = snapshot("loading") {
        LoadingIndicator(modifier = Modifier.fillMaxSize())
    }
}

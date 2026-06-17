package com.pzverkov.socialapp.screenshot.itemdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.captureRoboImage
import com.pzverkov.socialapp.core.ui.theme.SocialAppTheme
import com.pzverkov.socialapp.feature.itemdetail.presentation.AiSummarySection
import com.pzverkov.socialapp.feature.itemdetail.presentation.SummaryUiState
import com.pzverkov.socialapp.feature.itemdetail.presentation.TranslationSection
import com.pzverkov.socialapp.feature.itemdetail.presentation.TranslationUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshot tests for the on-device AI affordances on the detail screen: the summary chip/card
 * and the translation card, in their key states. Rendered by Roborazzi under Robolectric at SDK 36.
 *
 * Lives in its own module so its Robolectric coverage data never reaches the kover aggregate.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class CardScreenshotTest {

    private fun snapshot(name: String, content: @Composable () -> Unit) {
        captureRoboImage("src/test/screenshots/$name.png") {
            SocialAppTheme {
                Column(
                    modifier = Modifier
                        .width(360.dp)
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp),
                ) {
                    content()
                }
            }
        }
    }

    @Test
    fun summaryAvailable() = snapshot("summary_available") {
        AiSummarySection(summary = SummaryUiState.Available, onSummarizeClick = {})
    }

    @Test
    fun summaryReady() = snapshot("summary_ready") {
        AiSummarySection(
            summary = SummaryUiState.Ready(
                "- Vintage camera in great condition\n- Priced around 150 USD\n- Located in New York",
            ),
            onSummarizeClick = {},
        )
    }

    @Test
    fun translationAvailable() = snapshot("translation_available") {
        TranslationSection(
            translation = TranslationUiState.Available("es"),
            onTranslateClick = {},
            onShowOriginalClick = {},
        )
    }

    @Test
    fun translationTranslated() = snapshot("translation_translated") {
        TranslationSection(
            translation = TranslationUiState.Translated("Camara vintage en excelente estado"),
            onTranslateClick = {},
            onShowOriginalClick = {},
        )
    }
}

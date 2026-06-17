package com.pzverkov.socialapp.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates a baseline profile by exercising the hot paths: cold startup, the item-grid scroll,
 * and opening an item's detail. The baselineprofile Gradle plugin runs this on the managed device
 * and folds the result into the app's release profile, so AOT-compiled code covers startup and
 * the main scroll path the performance work targets.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: "com.pzverkov.socialapp",
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()

        // Scroll the item grid up and down to capture the list-render path.
        val grid = device.findObject(By.scrollable(true))
        if (grid != null) {
            grid.setGestureMargin(device.displayWidth / 5)
            repeat(2) { grid.fling(Direction.DOWN) }
            grid.fling(Direction.UP)
        }
        device.waitForIdle()

        // Open the first item's detail and return, to capture the detail path.
        device.findObject(By.scrollable(true))?.children?.firstOrNull()?.click()
        device.waitForIdle()
        device.pressBack()
        device.waitForIdle()
    }
}

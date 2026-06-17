package com.pzverkov.socialapp.feature.itemdetail.presentation

import android.graphics.Bitmap
import com.pzverkov.socialapp.core.ai.AiAvailability
import com.pzverkov.socialapp.core.ai.AiFeature
import com.pzverkov.socialapp.core.ai.AiResult
import com.pzverkov.socialapp.core.ai.OnDeviceAiClient

/**
 * Test double for [OnDeviceAiClient]. Defaults to UNAVAILABLE so existing tests see the same
 * behavior as a device without AICore; flip the fields to drive the AI paths.
 */
class FakeOnDeviceAiClient(
    var summarizationAvailability: AiAvailability = AiAvailability.UNAVAILABLE,
    var imageAvailability: AiAvailability = AiAvailability.UNAVAILABLE,
    var summarizeResult: AiResult<String> = AiResult.Unavailable,
    var describeResult: AiResult<String> = AiResult.Unavailable,
) : OnDeviceAiClient {

    override suspend fun availability(feature: AiFeature): AiAvailability = when (feature) {
        AiFeature.SUMMARIZATION -> summarizationAvailability
        AiFeature.IMAGE_DESCRIPTION -> imageAvailability
    }

    override suspend fun summarize(text: String): AiResult<String> = summarizeResult

    override suspend fun describeImage(bitmap: Bitmap): AiResult<String> = describeResult
}

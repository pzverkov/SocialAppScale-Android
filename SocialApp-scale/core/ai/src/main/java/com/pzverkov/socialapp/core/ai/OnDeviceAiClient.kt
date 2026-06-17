package com.pzverkov.socialapp.core.ai

import android.graphics.Bitmap

/** On-device generative-AI capabilities, each gated independently per device. */
enum class AiFeature { SUMMARIZATION, IMAGE_DESCRIPTION }

/**
 * Whether a feature can run now. [DOWNLOADABLE] means the device supports it but the model
 * is not yet on disk; the first inference will fetch it. [UNAVAILABLE] means the device cannot
 * run it at all (most devices) and callers should hide the affordance.
 */
enum class AiAvailability { AVAILABLE, DOWNLOADABLE, UNAVAILABLE }

sealed interface AiResult<out T> {
    data class Success<T>(val value: T) : AiResult<T>
    data object Unavailable : AiResult<Nothing>
    data class Failed(val cause: Throwable) : AiResult<Nothing>
}

/**
 * Wraps on-device Gemini Nano (ML Kit GenAI). Every call is safe on every device: unsupported
 * hardware returns [AiAvailability.UNAVAILABLE] / [AiResult.Unavailable] rather than throwing.
 */
interface OnDeviceAiClient {
    suspend fun availability(feature: AiFeature): AiAvailability
    suspend fun summarize(text: String): AiResult<String>
    suspend fun describeImage(bitmap: Bitmap): AiResult<String>
}

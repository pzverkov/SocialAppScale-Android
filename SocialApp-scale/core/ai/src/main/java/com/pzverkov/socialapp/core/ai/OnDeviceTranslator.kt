package com.pzverkov.socialapp.core.ai

/**
 * On-device translation via classic ML Kit. Unlike the Gemini Nano features this runs on
 * essentially all devices; the only cost is a one-time per-language-pair model download.
 */
interface OnDeviceTranslator {
    /** BCP-47 language tag detected for [text], or null if undetermined. */
    suspend fun detectLanguage(text: String): String?

    /**
     * Translates [text] from [sourceLanguageTag] to [targetLanguageTag], downloading the model
     * if needed. Returns [AiResult.Unavailable] when either language is unsupported.
     */
    suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
    ): AiResult<String>
}

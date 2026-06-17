package com.pzverkov.socialapp.feature.itemdetail.presentation

import com.pzverkov.socialapp.core.ai.AiResult
import com.pzverkov.socialapp.core.ai.OnDeviceTranslator

/**
 * Test double for [OnDeviceTranslator]. Defaults to "language undetermined", so by default no
 * translation is offered and existing tests are unaffected.
 */
class FakeOnDeviceTranslator(
    var detectedLanguage: String? = null,
    var translateResult: AiResult<String> = AiResult.Unavailable,
) : OnDeviceTranslator {

    override suspend fun detectLanguage(text: String): String? = detectedLanguage

    override suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
    ): AiResult<String> = translateResult
}

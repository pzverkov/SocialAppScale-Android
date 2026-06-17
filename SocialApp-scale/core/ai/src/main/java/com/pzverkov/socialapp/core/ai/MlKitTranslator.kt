package com.pzverkov.socialapp.core.ai

import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * ML Kit implementation of [OnDeviceTranslator]. Language identification and translation both
 * return GMS Tasks; `await()` bridges them to coroutines. Clients are created per call and
 * closed in a finally block. Unsupported languages or offline model downloads degrade to
 * [AiResult.Unavailable] / [AiResult.Failed] rather than throwing.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class MlKitTranslator : OnDeviceTranslator {

    override suspend fun detectLanguage(text: String): String? {
        val client = LanguageIdentification.getClient()
        return try {
            client.identifyLanguage(text).await().takeUnless { it == UNDETERMINED }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        } finally {
            client.close()
        }
    }

    override suspend fun translate(
        text: String,
        sourceLanguageTag: String,
        targetLanguageTag: String,
    ): AiResult<String> {
        val source = TranslateLanguage.fromLanguageTag(sourceLanguageTag)
        val target = TranslateLanguage.fromLanguageTag(targetLanguageTag)
        if (source == null || target == null) return AiResult.Unavailable

        val translator = Translation.getClient(
            TranslatorOptions.Builder()
                .setSourceLanguage(source)
                .setTargetLanguage(target)
                .build(),
        )
        return try {
            translator.downloadModelIfNeeded().await()
            AiResult.Success(translator.translate(text).await())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiResult.Failed(e)
        } finally {
            translator.close()
        }
    }

    private companion object {
        const val UNDETERMINED = "und"
    }
}

package com.pzverkov.socialapp.core.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.imagedescription.ImageDescription
import com.google.mlkit.genai.imagedescription.ImageDescriptionRequest
import com.google.mlkit.genai.imagedescription.ImageDescriber
import com.google.mlkit.genai.imagedescription.ImageDescriberOptions
import com.google.mlkit.genai.summarization.Summarization
import com.google.mlkit.genai.summarization.SummarizationRequest
import com.google.mlkit.genai.summarization.Summarizer
import com.google.mlkit.genai.summarization.SummarizerOptions
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * ML Kit GenAI implementation of [OnDeviceAiClient], backed by Gemini Nano through AICore.
 * Clients are created per call and closed in a finally block. On devices without the feature,
 * [FeatureStatus] reports UNAVAILABLE and the call returns [AiResult.Unavailable] - it never throws.
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class MlKitOnDeviceAiClient(
    private val context: Context,
) : OnDeviceAiClient {

    override suspend fun availability(feature: AiFeature): AiAvailability = try {
        when (feature) {
            AiFeature.SUMMARIZATION -> {
                val client = newSummarizer()
                try {
                    client.checkFeatureStatus().await().toAvailability()
                } finally {
                    client.close()
                }
            }
            AiFeature.IMAGE_DESCRIPTION -> {
                val client = newImageDescriber()
                try {
                    client.checkFeatureStatus().await().toAvailability()
                } finally {
                    client.close()
                }
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // No AICore / unsupported device: treat as unavailable rather than crashing the caller.
        AiAvailability.UNAVAILABLE
    }

    override suspend fun summarize(text: String): AiResult<String> = runCatchingAi {
        val client = newSummarizer()
        try {
            if (!client.ensureReady()) return@runCatchingAi AiResult.Unavailable
            val request = SummarizationRequest.builder(text).build()
            AiResult.Success(client.runInference(request).await().summary)
        } finally {
            client.close()
        }
    }

    override suspend fun describeImage(bitmap: Bitmap): AiResult<String> = runCatchingAi {
        val client = newImageDescriber()
        try {
            if (!client.ensureReady()) return@runCatchingAi AiResult.Unavailable
            val request = ImageDescriptionRequest.builder(bitmap).build()
            AiResult.Success(client.runInference(request).await().description)
        } finally {
            client.close()
        }
    }

    private fun newSummarizer(): Summarizer {
        val options = SummarizerOptions.builder(context)
            .setInputType(SummarizerOptions.InputType.ARTICLE)
            .setOutputType(SummarizerOptions.OutputType.THREE_BULLETS)
            .setLanguage(SummarizerOptions.Language.ENGLISH)
            .build()
        return Summarization.getClient(options)
    }

    private fun newImageDescriber(): ImageDescriber =
        ImageDescription.getClient(ImageDescriberOptions.builder(context).build())

    /** Downloads the model on first use if the device supports it but it is not yet on disk. */
    private suspend fun Summarizer.ensureReady(): Boolean = when (checkFeatureStatus().await()) {
        FeatureStatus.AVAILABLE -> true
        FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> awaitDownload { downloadFeature(it) }
        else -> false
    }

    private suspend fun ImageDescriber.ensureReady(): Boolean = when (checkFeatureStatus().await()) {
        FeatureStatus.AVAILABLE -> true
        FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> awaitDownload { downloadFeature(it) }
        else -> false
    }

    private fun Int.toAvailability(): AiAvailability = when (this) {
        FeatureStatus.AVAILABLE -> AiAvailability.AVAILABLE
        FeatureStatus.DOWNLOADABLE, FeatureStatus.DOWNLOADING -> AiAvailability.DOWNLOADABLE
        else -> AiAvailability.UNAVAILABLE
    }

    private inline fun runCatchingAi(block: () -> AiResult<String>): AiResult<String> =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AiResult.Failed(e)
        }

    private suspend fun awaitDownload(start: (DownloadCallback) -> Unit): Boolean =
        suspendCancellableCoroutine { cont ->
            start(object : DownloadCallback {
                override fun onDownloadStarted(bytesToDownload: Long) = Unit
                override fun onDownloadProgress(totalBytesDownloaded: Long) = Unit
                override fun onDownloadCompleted() { if (cont.isActive) cont.resume(true) }
                override fun onDownloadFailed(e: GenAiException) { if (cont.isActive) cont.resume(false) }
            })
        }
}

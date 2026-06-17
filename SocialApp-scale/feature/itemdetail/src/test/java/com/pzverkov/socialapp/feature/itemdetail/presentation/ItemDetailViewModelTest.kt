package com.pzverkov.socialapp.feature.itemdetail.presentation

import app.cash.turbine.test
import com.pzverkov.socialapp.core.ai.AiAvailability
import com.pzverkov.socialapp.core.ai.AiResult
import com.pzverkov.socialapp.core.domain.NetworkResult
import com.pzverkov.socialapp.core.sharing.InstallationIdProvider
import com.pzverkov.socialapp.core.sharing.ShareLinkBuilder
import com.pzverkov.socialapp.core.model.Item
import com.pzverkov.socialapp.core.testing.FakeFavoriteRepository
import com.pzverkov.socialapp.core.testing.FakeItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ItemDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeItemRepository
    private lateinit var fakeFavoriteRepository: FakeFavoriteRepository
    private lateinit var fakeAiClient: FakeOnDeviceAiClient
    private lateinit var fakeTranslator: FakeOnDeviceTranslator

    private val fakeIdProvider = object : InstallationIdProvider {
        override fun get() = "test1234"
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeItemRepository()
        fakeFavoriteRepository = FakeFavoriteRepository()
        fakeAiClient = FakeOnDeviceAiClient()
        fakeTranslator = FakeOnDeviceTranslator()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(itemId: Int = 1): ItemDetailViewModel {
        return ItemDetailViewModel(
            itemId = itemId,
            itemRepository = fakeRepository,
            favoriteRepository = fakeFavoriteRepository,
            shareLinkBuilder = ShareLinkBuilder(fakeIdProvider),
            aiClient = fakeAiClient,
            translator = fakeTranslator,
        )
    }

    private val foreignLanguage = if (Locale.getDefault().language == "es") "fr" else "es"

    @Test
    fun `loading transitions to loaded with correct item`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test {
            assertEquals(ItemDetailState.Loading, awaitItem())
            val loaded = awaitItem() as ItemDetailState.Loaded
            assertEquals("Camera", loaded.item.title)
            assertEquals("$150.00", loaded.item.formattedPrice)
        }
    }

    @Test
    fun `item not found results in error state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(emptyList())
        fakeRepository.itemResult = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.UNKNOWN)
        val viewModel = createViewModel(itemId = 999)

        viewModel.state.test {
            assertEquals(ItemDetailState.Loading, awaitItem())
            assertTrue(awaitItem() is ItemDetailState.Error)
        }
    }

    @Test
    fun `share click emits share event with deeplink`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test { skipItems(2) }

        viewModel.events.test {
            viewModel.onShareClicked()
            val event = awaitItem() as ItemDetailEvent.ShareItem
            assertTrue(event.text.contains("Camera"))
            assertTrue(event.text.contains("socialapp://item/1"))
        }
    }

    @Test
    fun `buy click emits message event`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test { skipItems(2) }

        viewModel.events.test {
            viewModel.onBuyClicked()
            assertTrue(awaitItem() is ItemDetailEvent.ShowPurchaseMessage)
        }
    }

    @Test
    fun `toggling favorite updates detail state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test {
            skipItems(1)
            assertFalse((awaitItem() as ItemDetailState.Loaded).item.isFavorite)

            viewModel.onFavoriteClicked()
            assertTrue((awaitItem() as ItemDetailState.Loaded).item.isFavorite)
        }
    }

    @Test
    fun `retry reloads item after error`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(emptyList())
        fakeRepository.itemResult = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.UNKNOWN)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test {
            skipItems(2) // Loading -> Error
            fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
            viewModel.retry()
            assertEquals(ItemDetailState.Loading, awaitItem())
            assertTrue(awaitItem() is ItemDetailState.Loaded)
        }
    }

    @Test
    fun `share and buy do nothing when not loaded`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.UNKNOWN)
        val viewModel = createViewModel(itemId = 1)

        viewModel.state.test { skipItems(2) }

        viewModel.onShareClicked()
        viewModel.onBuyClicked()
        // No crash, no event emitted
    }

    @Test
    fun `summary hidden when device lacks ai support`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        assertEquals(SummaryUiState.Hidden, (viewModel.state.value as ItemDetailState.Loaded).summary)
    }

    @Test
    fun `summary becomes available when device supports summarization`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeAiClient.summarizationAvailability = AiAvailability.AVAILABLE
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        assertEquals(SummaryUiState.Available, (viewModel.state.value as ItemDetailState.Loaded).summary)
    }

    @Test
    fun `summarize click produces ready summary`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeAiClient.summarizationAvailability = AiAvailability.AVAILABLE
        fakeAiClient.summarizeResult = AiResult.Success("Short summary")
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        viewModel.onSummarizeClicked()
        advanceUntilIdle()

        assertEquals(SummaryUiState.Ready("Short summary"), (viewModel.state.value as ItemDetailState.Loaded).summary)
    }

    @Test
    fun `summarize failure shows failed state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeAiClient.summarizationAvailability = AiAvailability.AVAILABLE
        fakeAiClient.summarizeResult = AiResult.Failed(RuntimeException("boom"))
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        viewModel.onSummarizeClicked()
        advanceUntilIdle()

        assertEquals(SummaryUiState.Failed, (viewModel.state.value as ItemDetailState.Loaded).summary)
    }

    @Test
    fun `image description capability enables alt text generation`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeAiClient.imageAvailability = AiAvailability.AVAILABLE
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        assertTrue((viewModel.state.value as ItemDetailState.Loaded).canDescribeImage)
    }

    @Test
    fun `translation hidden when description matches device language`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeTranslator.detectedLanguage = Locale.getDefault().language
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        assertEquals(TranslationUiState.Hidden, (viewModel.state.value as ItemDetailState.Loaded).translation)
    }

    @Test
    fun `translation offered when description language differs`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeTranslator.detectedLanguage = foreignLanguage
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        assertEquals(
            TranslationUiState.Available(foreignLanguage),
            (viewModel.state.value as ItemDetailState.Loaded).translation,
        )
    }

    @Test
    fun `translate click produces translated text`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeTranslator.detectedLanguage = foreignLanguage
        fakeTranslator.translateResult = AiResult.Success("Translated text")
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        viewModel.onTranslateClicked()
        advanceUntilIdle()

        assertEquals(
            TranslationUiState.Translated("Translated text"),
            (viewModel.state.value as ItemDetailState.Loaded).translation,
        )
    }

    @Test
    fun `translate failure shows failed state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeTranslator.detectedLanguage = foreignLanguage
        fakeTranslator.translateResult = AiResult.Failed(RuntimeException("boom"))
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        viewModel.onTranslateClicked()
        advanceUntilIdle()

        assertEquals(TranslationUiState.Failed, (viewModel.state.value as ItemDetailState.Loaded).translation)
    }

    @Test
    fun `show original reverts translation to available`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        fakeTranslator.detectedLanguage = foreignLanguage
        fakeTranslator.translateResult = AiResult.Success("Translated text")
        val viewModel = createViewModel(itemId = 1)
        advanceUntilIdle()

        viewModel.onTranslateClicked()
        advanceUntilIdle()
        viewModel.onShowOriginalClicked()

        assertEquals(
            TranslationUiState.Available(foreignLanguage),
            (viewModel.state.value as ItemDetailState.Loaded).translation,
        )
    }

    companion object {
        val sampleItems = listOf(
            Item(1, "Camera", "A vintage camera", 150.0, "https://example.com/camera.jpg", "New York"),
        )
    }
}

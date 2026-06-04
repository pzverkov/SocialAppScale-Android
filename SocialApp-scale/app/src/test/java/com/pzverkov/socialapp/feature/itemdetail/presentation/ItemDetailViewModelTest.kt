package com.pzverkov.socialapp.feature.itemdetail.presentation

import app.cash.turbine.test
import com.pzverkov.socialapp.core.domain.NetworkResult
import com.pzverkov.socialapp.core.sharing.InstallationIdProvider
import com.pzverkov.socialapp.core.sharing.ShareLinkBuilder
import com.pzverkov.socialapp.core.model.Item
import com.pzverkov.socialapp.feature.itemlist.presentation.FakeFavoriteRepository
import com.pzverkov.socialapp.feature.itemlist.presentation.FakeItemRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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

    private val fakeIdProvider = object : InstallationIdProvider {
        override fun get() = "test1234"
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeItemRepository()
        fakeFavoriteRepository = FakeFavoriteRepository()
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
        )
    }

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

    companion object {
        val sampleItems = listOf(
            Item(1, "Camera", "A vintage camera", 150.0, "https://example.com/camera.jpg", "New York"),
        )
    }
}

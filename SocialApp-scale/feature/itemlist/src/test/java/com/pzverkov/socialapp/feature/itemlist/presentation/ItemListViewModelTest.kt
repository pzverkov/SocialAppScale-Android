package com.pzverkov.socialapp.feature.itemlist.presentation

import app.cash.turbine.test
import com.pzverkov.socialapp.core.domain.NetworkResult
import com.pzverkov.socialapp.core.sharing.InstallationIdProvider
import com.pzverkov.socialapp.core.sharing.ShareLinkBuilder
import com.pzverkov.socialapp.core.model.Item
import com.pzverkov.socialapp.core.testing.FakeFavoriteRepository
import com.pzverkov.socialapp.core.testing.FakeItemRepository
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
class ItemListViewModelTest {

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

    private fun createViewModel(): ItemListViewModel {
        return ItemListViewModel(
            itemRepository = fakeRepository,
            favoriteRepository = fakeFavoriteRepository,
            shareLinkBuilder = ShareLinkBuilder(fakeIdProvider),
        )
    }

    @Test
    fun `initial state is loading then transitions to loaded`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(ItemListState.Loading, awaitItem())
            val loaded = awaitItem()
            assertTrue(loaded is ItemListState.Loaded)
            assertEquals(2, (loaded as ItemListState.Loaded).items.size)
        }
    }

    @Test
    fun `empty list results in empty state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(emptyList())
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(ItemListState.Loading, awaitItem())
            assertEquals(ItemListState.Empty, awaitItem())
        }
    }

    @Test
    fun `network error results in error state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.NETWORK)
        val viewModel = createViewModel()

        viewModel.state.test {
            assertEquals(ItemListState.Loading, awaitItem())
            val error = awaitItem()
            assertTrue(error is ItemListState.Error)
            assertEquals(com.pzverkov.socialapp.core.model.ErrorType.NETWORK, (error as ItemListState.Error).errorType)
        }
    }

    @Test
    fun `retry reloads items after error`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.NETWORK)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2) // Loading -> Error
            fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
            viewModel.loadItems()
            assertEquals(ItemListState.Loading, awaitItem())
            assertTrue(awaitItem() is ItemListState.Loaded)
        }
    }

    @Test
    fun `item click emits navigation event`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.events.test {
            viewModel.onItemClicked(1)
            assertEquals(ItemListEvent.NavigateToDetail(1), awaitItem())
        }
    }

    @Test
    fun `loaded items have correct formatted price`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(1)
            val loaded = awaitItem() as ItemListState.Loaded
            assertEquals("$150.00", loaded.items[0].formattedPrice)
        }
    }

    @Test
    fun `toggling favorite updates item state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(1)
            val initial = awaitItem() as ItemListState.Loaded
            assertFalse(initial.items[0].isFavorite)

            viewModel.onFavoriteClicked(1)
            val updated = awaitItem() as ItemListState.Loaded
            assertTrue(updated.items[0].isFavorite)
        }
    }

    @Test
    fun `search filters items by title`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onSearchQueryChanged("Camera")
            val filtered = awaitItem() as ItemListState.Loaded
            assertEquals(1, filtered.items.size)
            assertEquals("Camera", filtered.items[0].title)
        }
    }

    @Test
    fun `search filters items by location`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onSearchQueryChanged("London")
            val filtered = awaitItem() as ItemListState.Loaded
            assertEquals(1, filtered.items.size)
            assertEquals("Bike", filtered.items[0].title)
        }
    }

    @Test
    fun `clearing search shows all items`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onSearchQueryChanged("Camera")
            awaitItem()
            viewModel.onSearchQueryChanged("")
            val all = awaitItem() as ItemListState.Loaded
            assertEquals(2, all.items.size)
        }
    }

    @Test
    fun `search with no matches shows SearchEmpty state`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onSearchQueryChanged("nonexistent")
            val empty = awaitItem()
            assertTrue(empty is ItemListState.SearchEmpty)
            assertEquals("nonexistent", (empty as ItemListState.SearchEmpty).query)
        }
    }

    @Test
    fun `filter favorites shows only favorited items`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onFavoriteClicked(1)
            awaitItem()
            viewModel.onFilterChanged(ItemFilter.FAVORITES)
            val filtered = awaitItem() as ItemListState.Loaded
            assertEquals(1, filtered.items.size)
            assertEquals("Camera", filtered.items[0].title)
        }
    }

    @Test
    fun `filter favorites with none shows empty`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test {
            skipItems(2)
            viewModel.onFilterChanged(ItemFilter.FAVORITES)
            assertTrue(awaitItem() is ItemListState.Empty)
        }
    }

    @Test
    fun `grid toggle switches between 1 and 2 columns`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        assertEquals(2, viewModel.gridColumns.value)
        viewModel.toggleGridColumns()
        assertEquals(1, viewModel.gridColumns.value)
        viewModel.toggleGridColumns()
        assertEquals(2, viewModel.gridColumns.value)
    }

    @Test
    fun `share emits event with deeplink`() = runTest(testDispatcher) {
        fakeRepository.itemsResult = NetworkResult.Success(sampleItems)
        val viewModel = createViewModel()

        viewModel.state.test { skipItems(2) }

        val item = (viewModel.state.value as ItemListState.Loaded).items[0]
        viewModel.events.test {
            viewModel.onShareClicked(item)
            val event = awaitItem()
            assertTrue(event is ItemListEvent.ShareItem)
            assertTrue((event as ItemListEvent.ShareItem).text.contains("socialapp://"))
        }
    }

    companion object {
        val sampleItems = listOf(
            Item(1, "Camera", "A vintage camera", 150.0, "https://example.com/camera.jpg", "New York"),
            Item(2, "Bike", "A mountain bike", 450.0, "https://example.com/bike.jpg", "London"),
        )
    }
}

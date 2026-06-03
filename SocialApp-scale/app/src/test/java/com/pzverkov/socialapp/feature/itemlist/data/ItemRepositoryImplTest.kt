package com.pzverkov.socialapp.feature.itemlist.data

import com.pzverkov.socialapp.core.network.ErrorType
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.feature.itemlist.data.dto.ItemDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class ItemRepositoryImplTest {

    private lateinit var fakeApi: FakeSocialAppApi
    private lateinit var repository: ItemRepositoryImpl

    @Before
    fun setup() {
        fakeApi = FakeSocialAppApi()
        repository = ItemRepositoryImpl(fakeApi)
    }

    @Test
    fun `getItems returns items from api`() = runTest {
        fakeApi.itemsResponse = listOf(sampleDto(1), sampleDto(2))

        val result = repository.getItems()

        assertTrue(result is NetworkResult.Success)
        assertEquals(2, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getItems caches after first call`() = runTest {
        fakeApi.itemsResponse = listOf(sampleDto(1))
        repository.getItems()

        fakeApi.itemsResponse = listOf(sampleDto(1), sampleDto(2), sampleDto(3))
        val result = repository.getItems()

        // Should return cached (1 item), not refreshed (3 items)
        assertEquals(1, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getItems forceRefresh bypasses cache`() = runTest {
        fakeApi.itemsResponse = listOf(sampleDto(1))
        repository.getItems()

        fakeApi.itemsResponse = listOf(sampleDto(1), sampleDto(2))
        val result = repository.getItems(forceRefresh = true)

        assertEquals(2, (result as NetworkResult.Success).data.size)
    }

    @Test
    fun `getItems returns NETWORK error on IOException`() = runTest {
        fakeApi.shouldThrow = IOException("No connection")

        val result = repository.getItems()

        assertTrue(result is NetworkResult.Error)
        assertEquals(ErrorType.NETWORK, (result as NetworkResult.Error).type)
    }

    @Test
    fun `getItems returns UNKNOWN error for generic exceptions`() = runTest {
        fakeApi.shouldThrow = RuntimeException("some internal error")

        val result = repository.getItems()

        assertTrue(result is NetworkResult.Error)
        assertEquals(ErrorType.UNKNOWN, (result as NetworkResult.Error).type)
    }

    @Test(expected = CancellationException::class)
    fun `getItems rethrows CancellationException`() = runTest {
        fakeApi.shouldThrow = CancellationException("cancelled")
        repository.getItems()
    }

    @Test
    fun `getItem returns from cache when available`() = runTest {
        fakeApi.itemsResponse = listOf(sampleDto(1), sampleDto(2))
        repository.getItems() // populate cache

        fakeApi.shouldThrow = IOException("should not be called")
        val result = repository.getItem(1)

        assertTrue(result is NetworkResult.Success)
        assertEquals(1, (result as NetworkResult.Success).data.id)
    }

    @Test
    fun `getItem falls back to api when not in cache`() = runTest {
        fakeApi.singleItemResponse = sampleDto(5)

        val result = repository.getItem(5)

        assertTrue(result is NetworkResult.Success)
        assertEquals(5, (result as NetworkResult.Success).data.id)
    }

    @Test
    fun `getItem returns error when api fails and not cached`() = runTest {
        fakeApi.shouldThrow = IOException("timeout")

        val result = repository.getItem(1)

        assertTrue(result is NetworkResult.Error)
    }

    @Test(expected = CancellationException::class)
    fun `getItem rethrows CancellationException`() = runTest {
        fakeApi.shouldThrow = CancellationException("cancelled")
        repository.getItem(1)
    }

    private fun sampleDto(id: Int) = ItemDto(
        id = id,
        title = "Item $id",
        description = "Description $id",
        price = 100.0 * id,
        imageUrl = "",
        location = "New York",
    )
}

private class FakeSocialAppApi : SocialAppApi {
    var itemsResponse: List<ItemDto> = emptyList()
    var singleItemResponse: ItemDto? = null
    var shouldThrow: Exception? = null

    override suspend fun getItems(): List<ItemDto> {
        shouldThrow?.let { throw it }
        return itemsResponse
    }

    override suspend fun getItem(id: Int): ItemDto {
        shouldThrow?.let { throw it }
        return singleItemResponse ?: itemsResponse.first { it.id == id }
    }
}

package com.pzverkov.socialapp.feature.favorite.data

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FavoriteRepositoryImplTest {

    private lateinit var fakeDao: FakeFavoriteDao
    private lateinit var repository: FavoriteRepositoryImpl

    @Before
    fun setup() {
        fakeDao = FakeFavoriteDao()
        repository = FavoriteRepositoryImpl(fakeDao)
    }

    @Test
    fun `observeFavoriteIds emits set of ids`() = runTest {
        fakeDao.addFavorite(1)
        fakeDao.addFavorite(3)

        repository.observeFavoriteIds().test {
            assertEquals(setOf(1, 3), awaitItem())
        }
    }

    @Test
    fun `toggleFavorite adds when not favorited`() = runTest {
        assertFalse(repository.isFavorite(1))

        repository.toggleFavorite(1)

        assertTrue(repository.isFavorite(1))
    }

    @Test
    fun `toggleFavorite removes when already favorited`() = runTest {
        fakeDao.addFavorite(1)
        assertTrue(repository.isFavorite(1))

        repository.toggleFavorite(1)

        assertFalse(repository.isFavorite(1))
    }

    @Test
    fun `isFavorite returns false for unknown id`() = runTest {
        assertFalse(repository.isFavorite(999))
    }

    @Test
    fun `toggleFavorite twice returns to original state`() = runTest {
        repository.toggleFavorite(1)
        repository.toggleFavorite(1)

        assertFalse(repository.isFavorite(1))
    }

    @Test
    fun `observeFavoriteIds updates after toggle`() = runTest {
        repository.observeFavoriteIds().test {
            assertEquals(emptySet<Int>(), awaitItem())

            fakeDao.addFavorite(5)
            assertEquals(setOf(5), awaitItem())
        }
    }
}

private class FakeFavoriteDao : FavoriteDao {
    private val favorites = MutableStateFlow<List<Int>>(emptyList())

    fun addFavorite(id: Int) {
        favorites.value = favorites.value + id
    }

    override fun observeAll(): Flow<List<Int>> = favorites

    override suspend fun insert(entity: FavoriteEntity) {
        favorites.value = favorites.value + entity.itemId
    }

    override suspend fun delete(itemId: Int) {
        favorites.value = favorites.value - itemId
    }

    override suspend fun isFavorite(itemId: Int): Boolean {
        return itemId in favorites.value
    }
}

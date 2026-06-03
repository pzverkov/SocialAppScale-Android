package com.pzverkov.socialapp.feature.favorite.data

import com.pzverkov.socialapp.feature.favorite.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepositoryImpl @Inject constructor(
    private val dao: FavoriteDao,
) : FavoriteRepository {

    override fun observeFavoriteIds(): Flow<Set<Int>> {
        return dao.observeAll().map { it.toSet() }
    }

    override suspend fun toggleFavorite(itemId: Int) {
        if (dao.isFavorite(itemId)) {
            dao.delete(itemId)
        } else {
            dao.insert(FavoriteEntity(itemId))
        }
    }

    override suspend fun isFavorite(itemId: Int): Boolean {
        return dao.isFavorite(itemId)
    }
}

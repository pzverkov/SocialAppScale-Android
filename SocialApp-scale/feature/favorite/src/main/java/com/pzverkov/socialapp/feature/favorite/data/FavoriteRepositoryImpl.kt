package com.pzverkov.socialapp.feature.favorite.data

import com.pzverkov.socialapp.core.domain.FavoriteRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class FavoriteRepositoryImpl(
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

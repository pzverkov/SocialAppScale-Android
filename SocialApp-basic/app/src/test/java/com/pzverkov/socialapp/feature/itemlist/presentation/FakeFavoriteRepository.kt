package com.pzverkov.socialapp.feature.itemlist.presentation

import com.pzverkov.socialapp.feature.favorite.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoriteRepository : FavoriteRepository {

    private val favorites = MutableStateFlow<Set<Int>>(emptySet())

    override fun observeFavoriteIds(): Flow<Set<Int>> = favorites

    override suspend fun toggleFavorite(itemId: Int) {
        val current = favorites.value
        favorites.value = if (itemId in current) current - itemId else current + itemId
    }

    override suspend fun isFavorite(itemId: Int): Boolean = itemId in favorites.value
}

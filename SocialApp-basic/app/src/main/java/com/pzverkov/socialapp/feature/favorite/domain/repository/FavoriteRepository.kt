package com.pzverkov.socialapp.feature.favorite.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(itemId: Int)
    suspend fun isFavorite(itemId: Int): Boolean
}

package com.pzverkov.socialapp.core.domain

import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavoriteIds(): Flow<Set<Int>>
    suspend fun toggleFavorite(itemId: Int)
    suspend fun isFavorite(itemId: Int): Boolean
}

package com.pzverkov.socialapp.core.domain

import com.pzverkov.socialapp.core.model.Item

interface ItemRepository {
    suspend fun getItems(forceRefresh: Boolean = false): NetworkResult<List<Item>>
    suspend fun getItem(id: Int): NetworkResult<Item>
}

package com.pzverkov.socialapp.feature.itemlist.domain.repository

import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item

interface ItemRepository {
    suspend fun getItems(forceRefresh: Boolean = false): NetworkResult<List<Item>>
    suspend fun getItem(id: Int): NetworkResult<Item>
}

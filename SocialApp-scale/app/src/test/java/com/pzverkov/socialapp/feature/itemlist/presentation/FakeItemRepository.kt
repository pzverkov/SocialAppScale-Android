package com.pzverkov.socialapp.feature.itemlist.presentation

import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.core.model.Item
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository

class FakeItemRepository : ItemRepository {

    var itemsResult: NetworkResult<List<Item>> = NetworkResult.Success(emptyList())
    var itemResult: NetworkResult<Item> = NetworkResult.Error(com.pzverkov.socialapp.core.model.ErrorType.UNKNOWN)

    override suspend fun getItems(forceRefresh: Boolean): NetworkResult<List<Item>> = itemsResult

    override suspend fun getItem(id: Int): NetworkResult<Item> {
        val successResult = itemsResult
        if (successResult is NetworkResult.Success) {
            val found = successResult.data.find { it.id == id }
            if (found != null) return NetworkResult.Success(found)
        }
        return itemResult
    }
}

package com.pzverkov.socialapp

import com.pzverkov.socialapp.core.network.ErrorType
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.feature.itemlist.data.ItemRepositoryImpl
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@ContributesBinding(AppScope::class, replaces = [ItemRepositoryImpl::class])
@SingleIn(AppScope::class)
@Inject
class FakeItemRepositoryImpl : ItemRepository {

    var itemsResult: NetworkResult<List<Item>> = NetworkResult.Success(defaultItems)

    override suspend fun getItems(forceRefresh: Boolean): NetworkResult<List<Item>> = itemsResult

    override suspend fun getItem(id: Int): NetworkResult<Item> {
        val success = itemsResult
        if (success is NetworkResult.Success) {
            val found = success.data.find { it.id == id }
            if (found != null) return NetworkResult.Success(found)
        }
        return NetworkResult.Error(ErrorType.UNKNOWN)
    }

    companion object {
        val defaultItems = listOf(
            Item(
                id = 1,
                title = "Vintage Camera",
                description = "A beautiful vintage camera in excellent condition",
                price = 150.0,
                imageUrl = "",
                location = "New York",
            ),
            Item(
                id = 2,
                title = "Mountain Bike",
                description = "Trek mountain bike, barely used",
                price = 450.0,
                imageUrl = "",
                location = "London",
            ),
            Item(
                id = 3,
                title = "iPhone 13 Pro",
                description = "iPhone 13 Pro in Sierra Blue",
                price = 699.0,
                imageUrl = "",
                location = "New York",
            ),
        )
    }
}

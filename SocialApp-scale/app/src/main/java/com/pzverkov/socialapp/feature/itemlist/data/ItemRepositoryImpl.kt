package com.pzverkov.socialapp.feature.itemlist.data

import com.pzverkov.socialapp.core.network.ErrorType
import com.pzverkov.socialapp.core.network.NetworkResult
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class ItemRepositoryImpl(
    private val api: SocialAppApi,
) : ItemRepository {

    private val cacheMutex = Mutex()
    private var cachedItems: List<Item>? = null

    override suspend fun getItems(forceRefresh: Boolean): NetworkResult<List<Item>> {
        if (!forceRefresh) {
            cacheMutex.withLock {
                cachedItems?.let { return NetworkResult.Success(it) }
            }
        }

        return safeApiCall {
            val items = api.getItems().map { it.toDomain() }
            cacheMutex.withLock { cachedItems = items }
            items
        }
    }

    override suspend fun getItem(id: Int): NetworkResult<Item> {
        cacheMutex.withLock {
            cachedItems?.find { it.id == id }?.let { return NetworkResult.Success(it) }
        }

        return safeApiCall {
            api.getItem(id).toDomain()
        }
    }

    private suspend fun <T> safeApiCall(block: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            NetworkResult.Error(ErrorType.NETWORK)
        } catch (e: Exception) {
            NetworkResult.Error(ErrorType.UNKNOWN)
        }
    }
}

package com.pzverkov.socialapp

import androidx.test.core.app.ApplicationProvider
import com.pzverkov.socialapp.core.domain.NetworkResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * Resets process-global state that leaks between instrumentation tests: the fake [ItemRepository]
 * is an AppScope singleton (one instance for the whole process), and favorites live in a real Room
 * store that persists across cases. Without this, an Error set by one test or a favorite toggled by
 * another bleeds into the next.
 */
fun resetAppState() {
    val graph = ApplicationProvider.getApplicationContext<TestApplication>().graph as TestAppGraph
    (graph.itemRepository as FakeItemRepositoryImpl).itemsResult =
        NetworkResult.Success(FakeItemRepositoryImpl.defaultItems)
    runBlocking {
        graph.favoriteRepository.observeFavoriteIds().first().forEach { id ->
            graph.favoriteRepository.toggleFavorite(id)
        }
    }
}

/**
 * Runs [resetAppState] before each test. Order it OUTSIDE the compose rule (`@get:Rule(order = 0)`)
 * so the reset lands before the activity launches and reads the repository.
 */
class ResetAppStateRule : TestWatcher() {
    override fun starting(description: Description) = resetAppState()
}

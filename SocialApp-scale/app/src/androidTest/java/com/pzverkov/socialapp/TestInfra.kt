package com.pzverkov.socialapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.pzverkov.socialapp.core.di.AppGraphContract
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Instrumentation graph: aggregates the same AppScope contributions as production, but the
 * androidTest source set contributes [FakeItemRepositoryImpl] with `replaces =
 * [ItemRepositoryImpl::class]`, so the fake is bound instead of the real repository. Exposes
 * [itemRepository] so tests can drive it.
 */
@DependencyGraph(AppScope::class)
interface TestAppGraph : AppGraphContract, ViewModelGraph {

    val itemRepository: ItemRepository

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): TestAppGraph
    }
}

/** Application used under instrumentation; swaps the production graph for [TestAppGraph]. */
class TestApplication : SocialAppApplication() {
    override val graph: AppGraphContract by lazy { createGraphFactory<TestAppGraph.Factory>().create(this) }
}

/** Runner that installs [TestApplication] for instrumentation tests. */
class MetroTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestApplication::class.java.name, context)
    }
}

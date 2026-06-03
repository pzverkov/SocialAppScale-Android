package com.pzverkov.socialapp.core.di

import android.app.Application
import android.content.Context
import coil.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Accessor surface the app needs from its graph. A plain interface (not a @DependencyGraph) so both
 * the production [AppGraph] and the instrumentation test graph can satisfy it - Metro forbids one
 * @DependencyGraph extending another.
 */
interface AppGraphContract {
    /** The injected [MetroViewModelFactory] used by metroViewModel()/assistedMetroViewModel(). */
    val viewModelFactory: MetroViewModelFactory

    /** Coil image loader, consumed by the Application's ImageLoaderFactory. */
    val imageLoader: ImageLoader
}

/** Maps the factory-provided [Application] to [Context]; aggregated into any AppScope graph. */
@ContributesTo(AppScope::class)
interface ContextProvider {
    @Provides
    fun provideApplicationContext(application: Application): Context = application
}

/**
 * The application-scoped Metro graph. Providers, bindings, and ViewModels contribute into it via
 * @ContributesTo / @ContributesBinding / @ContributesIntoMap(AppScope), so this stays thin.
 * Created once in [com.pzverkov.socialapp.SocialAppApplication].
 */
@DependencyGraph(AppScope::class)
interface AppGraph : AppGraphContract, ViewModelGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}

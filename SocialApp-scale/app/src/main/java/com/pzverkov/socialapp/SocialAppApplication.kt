package com.pzverkov.socialapp

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.pzverkov.socialapp.core.di.AppGraph
import com.pzverkov.socialapp.core.di.AppGraphContract
import dev.zacsweers.metro.createGraphFactory

open class SocialAppApplication : Application(), SingletonImageLoader.Factory {

    open val graph: AppGraphContract by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override fun newImageLoader(context: PlatformContext): ImageLoader = graph.imageLoader
}

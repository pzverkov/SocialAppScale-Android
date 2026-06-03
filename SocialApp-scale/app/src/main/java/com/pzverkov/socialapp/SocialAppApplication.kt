package com.pzverkov.socialapp

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.pzverkov.socialapp.core.di.AppGraph
import com.pzverkov.socialapp.core.di.AppGraphContract
import dev.zacsweers.metro.createGraphFactory

open class SocialAppApplication : Application(), ImageLoaderFactory {

    open val graph: AppGraphContract by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override fun newImageLoader(): ImageLoader = graph.imageLoader
}

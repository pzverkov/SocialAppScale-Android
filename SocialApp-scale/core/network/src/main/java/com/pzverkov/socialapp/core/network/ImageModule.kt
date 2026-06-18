package com.pzverkov.socialapp.core.network

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath

@ContributesTo(AppScope::class)
interface ImageProviders {

    @Provides
    @SingleIn(AppScope::class)
    fun provideImageLoader(context: Context, client: OkHttpClient): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                // Reuse the app's single OkHttpClient so image traffic shares its connection pool,
                // dispatcher, and timeouts instead of spinning up a second network stack.
                add(OkHttpNetworkFetcherFactory(callFactory = { client }))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(300)
            .build()
    }
}

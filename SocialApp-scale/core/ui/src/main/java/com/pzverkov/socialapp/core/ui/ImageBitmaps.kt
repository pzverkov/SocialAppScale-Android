package com.pzverkov.socialapp.core.ui

import android.content.Context
import android.graphics.Bitmap
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware

/**
 * Loads [url] through the shared Coil image loader and returns a software [Bitmap], or null on
 * failure. `allowHardware(false)` forces a readable bitmap so on-device AI (ML Kit) can access
 * its pixels - hardware bitmaps cannot be read back. Reuses Coil's cache, so an image already
 * shown on screen is not re-fetched.
 */
suspend fun loadBitmap(context: Context, url: String): Bitmap? {
    val request = ImageRequest.Builder(context)
        .data(url)
        .allowHardware(false)
        .build()
    val result = context.imageLoader.execute(request) as? SuccessResult ?: return null
    return (result.image as? BitmapImage)?.bitmap
}

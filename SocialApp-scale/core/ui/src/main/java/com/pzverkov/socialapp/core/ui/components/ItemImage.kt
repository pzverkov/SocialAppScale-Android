package com.pzverkov.socialapp.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import com.pzverkov.socialapp.core.ui.theme.Divider

private val PlaceholderPainter = ColorPainter(Divider)
private val ImageSize = Size(400, 400)

@Composable
fun ItemImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    val context = LocalContext.current
    val model = remember(imageUrl, context) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(ImageSize)
            .crossfade(300)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        placeholder = PlaceholderPainter,
        error = PlaceholderPainter,
    )
}

package com.pzverkov.socialapp.feature.itemdetail.presentation

import androidx.compose.runtime.Immutable
import com.pzverkov.socialapp.core.model.Item
import com.pzverkov.socialapp.core.format.formatPrice

@Immutable
data class ItemDetailUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val imageUrl: String,
    val location: String,
    val sellerName: String,
    val isFavorite: Boolean = false,
)

fun Item.toDetailUiModel(isFavorite: Boolean = false): ItemDetailUiModel = ItemDetailUiModel(
    id = id,
    title = title,
    description = description,
    formattedPrice = formatPrice(price),
    imageUrl = imageUrl,
    location = location,
    sellerName = "SocialUser$id",
    isFavorite = isFavorite,
)

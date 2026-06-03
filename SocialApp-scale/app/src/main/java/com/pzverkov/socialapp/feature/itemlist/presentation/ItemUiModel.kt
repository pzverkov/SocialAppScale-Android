package com.pzverkov.socialapp.feature.itemlist.presentation

import androidx.compose.runtime.Immutable
import com.pzverkov.socialapp.core.format.formatPrice
import com.pzverkov.socialapp.feature.itemlist.domain.model.Item

@Immutable
data class ItemUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val imageUrl: String,
    val location: String,
    val isFavorite: Boolean = false,
)

fun Item.toUiModel(isFavorite: Boolean = false): ItemUiModel = ItemUiModel(
    id = id,
    title = title,
    description = description,
    formattedPrice = formatPrice(price),
    imageUrl = imageUrl,
    location = location,
    isFavorite = isFavorite,
)

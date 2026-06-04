package com.pzverkov.socialapp.feature.itemlist.data.dto

import com.pzverkov.socialapp.core.model.Item
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val location: String,
) {
    fun toDomain(): Item = Item(
        id = id,
        title = title,
        description = description,
        price = price,
        imageUrl = imageUrl,
        location = location,
    )
}

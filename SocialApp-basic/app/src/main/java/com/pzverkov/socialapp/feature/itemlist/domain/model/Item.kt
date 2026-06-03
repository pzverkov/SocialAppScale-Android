package com.pzverkov.socialapp.feature.itemlist.domain.model

data class Item(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val location: String,
)

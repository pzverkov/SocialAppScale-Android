package com.pzverkov.socialapp.feature.favorite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val itemId: Int,
)

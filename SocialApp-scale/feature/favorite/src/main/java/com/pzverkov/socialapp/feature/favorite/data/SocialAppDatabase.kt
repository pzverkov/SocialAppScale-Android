package com.pzverkov.socialapp.feature.favorite.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteEntity::class], version = 1, exportSchema = false)
abstract class SocialAppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}

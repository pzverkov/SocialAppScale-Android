package com.pzverkov.socialapp.feature.favorite.data

import android.content.Context
import androidx.room.Room
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DatabaseProviders {

    @Provides
    @SingleIn(AppScope::class)
    fun provideDatabase(context: Context): SocialAppDatabase {
        return Room.databaseBuilder(
            context,
            SocialAppDatabase::class.java,
            "socialapp.db",
        ).build()
    }

    @Provides
    fun provideFavoriteDao(database: SocialAppDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}

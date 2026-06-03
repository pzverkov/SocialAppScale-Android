package com.pzverkov.socialapp.feature.favorite.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SocialAppDatabase {
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

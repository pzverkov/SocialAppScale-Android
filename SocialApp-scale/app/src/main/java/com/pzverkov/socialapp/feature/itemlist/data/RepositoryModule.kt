package com.pzverkov.socialapp.feature.itemlist.data

import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository

    companion object {
        @Provides
        @Singleton
        fun provideSocialAppApi(retrofit: Retrofit): SocialAppApi {
            return retrofit.create(SocialAppApi::class.java)
        }
    }
}

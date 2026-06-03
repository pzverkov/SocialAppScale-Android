package com.pzverkov.socialapp

import com.pzverkov.socialapp.feature.itemlist.data.RepositoryModule
import com.pzverkov.socialapp.feature.itemlist.domain.repository.ItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class],
)
abstract class TestRepositoryModule {

    @Binds
    abstract fun bindItemRepository(impl: FakeItemRepositoryImpl): ItemRepository
}

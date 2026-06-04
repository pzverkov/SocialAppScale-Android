package com.pzverkov.socialapp.feature.itemlist.data

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import retrofit2.Retrofit

@ContributesTo(AppScope::class)
interface ItemDataProviders {

    @Provides
    @SingleIn(AppScope::class)
    fun provideSocialAppApi(retrofit: Retrofit): SocialAppApi {
        return retrofit.create(SocialAppApi::class.java)
    }
}

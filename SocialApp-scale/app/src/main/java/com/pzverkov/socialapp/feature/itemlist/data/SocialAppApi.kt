package com.pzverkov.socialapp.feature.itemlist.data

import com.pzverkov.socialapp.feature.itemlist.data.dto.ItemDto
import retrofit2.http.GET
import retrofit2.http.Path

interface SocialAppApi {

    @GET("/items")
    suspend fun getItems(): List<ItemDto>

    @GET("/items/{id}")
    suspend fun getItem(@Path("id") id: Int): ItemDto
}

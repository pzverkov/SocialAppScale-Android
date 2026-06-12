package com.pzverkov.socialapp.feature.itemlist.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.pzverkov.socialapp.feature.itemlist.presentation.ItemListScreen
import kotlinx.serialization.Serializable

@Serializable
object ItemListRoute

fun NavGraphBuilder.itemListScreen(onNavigateToDetail: (Int) -> Unit) {
    composable<ItemListRoute> {
        ItemListScreen(onNavigateToDetail = onNavigateToDetail)
    }
}

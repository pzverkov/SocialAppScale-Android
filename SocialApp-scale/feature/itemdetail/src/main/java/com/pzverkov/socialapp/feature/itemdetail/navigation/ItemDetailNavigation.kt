package com.pzverkov.socialapp.feature.itemdetail.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.pzverkov.socialapp.core.navigation.DEEPLINK_SCHEME
import com.pzverkov.socialapp.core.navigation.DEEPLINK_WEB_BASE
import com.pzverkov.socialapp.feature.itemdetail.presentation.ItemDetailScreen
import kotlinx.serialization.Serializable

@Serializable
data class ItemDetailRoute(val itemId: Int)

fun NavController.navigateToItemDetail(itemId: Int) = navigate(ItemDetailRoute(itemId))

/**
 * Registers the item detail destination. The route is type-safe; the deeplinks stay as explicit URI
 * patterns so they match the manifest's `scheme=socialapp host=item` shape exactly, and toRoute()
 * rebuilds the route from the parsed itemId.
 */
fun NavGraphBuilder.itemDetailScreen(onNavigateBack: () -> Unit) {
    composable<ItemDetailRoute>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "$DEEPLINK_SCHEME://item/{itemId}" },
            navDeepLink { uriPattern = "$DEEPLINK_WEB_BASE/item/{itemId}" },
        ),
    ) { entry ->
        val route = entry.toRoute<ItemDetailRoute>()
        ItemDetailScreen(
            itemId = route.itemId,
            onNavigateBack = onNavigateBack,
        )
    }
}

package com.pzverkov.socialapp.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.pzverkov.socialapp.feature.itemdetail.presentation.ItemDetailScreen
import com.pzverkov.socialapp.feature.itemlist.presentation.ItemListScreen

object Routes {
    const val ITEM_LIST = "items"
    const val ITEM_DETAIL = "items/{itemId}"

    fun itemDetail(itemId: Int) = "items/$itemId"
}

private const val DURATION = 280

@Composable
fun SocialAppNavHost() {
    val navController = rememberNavController()
    val spec = tween<IntOffset>(DURATION, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(DURATION, easing = FastOutSlowInEasing)

    NavHost(
        navController = navController,
        startDestination = Routes.ITEM_LIST,
        enterTransition = {
            scaleIn(
                initialScale = 0.92f,
                animationSpec = fadeSpec,
            ) + fadeIn(animationSpec = fadeSpec)
        },
        exitTransition = {
            scaleOut(
                targetScale = 1.04f,
                animationSpec = fadeSpec,
            ) + fadeOut(animationSpec = tween(DURATION / 2))
        },
        popEnterTransition = {
            scaleIn(
                initialScale = 1.04f,
                animationSpec = fadeSpec,
            ) + fadeIn(animationSpec = fadeSpec)
        },
        popExitTransition = {
            scaleOut(
                targetScale = 0.92f,
                animationSpec = fadeSpec,
            ) + fadeOut(animationSpec = tween(DURATION / 2))
        },
    ) {
        composable(Routes.ITEM_LIST) {
            ItemListScreen(
                onNavigateToDetail = { itemId ->
                    navController.navigate(Routes.itemDetail(itemId))
                },
            )
        }
        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "socialapp://item/{itemId}" },
                navDeepLink { uriPattern = "https://socialapp.app/item/{itemId}" },
            ),
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            ItemDetailScreen(
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

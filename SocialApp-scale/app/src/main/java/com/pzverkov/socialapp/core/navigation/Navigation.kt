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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.pzverkov.socialapp.feature.itemdetail.navigation.itemDetailScreen
import com.pzverkov.socialapp.feature.itemdetail.navigation.navigateToItemDetail
import com.pzverkov.socialapp.feature.itemlist.navigation.ItemListRoute
import com.pzverkov.socialapp.feature.itemlist.navigation.itemListScreen

private const val DURATION = 280

@Composable
fun SocialAppNavHost() {
    val navController = rememberNavController()
    val spec = tween<IntOffset>(DURATION, easing = FastOutSlowInEasing)
    val fadeSpec = tween<Float>(DURATION, easing = FastOutSlowInEasing)

    NavHost(
        navController = navController,
        startDestination = ItemListRoute,
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
        itemListScreen(
            onNavigateToDetail = { itemId -> navController.navigateToItemDetail(itemId) },
        )
        itemDetailScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}

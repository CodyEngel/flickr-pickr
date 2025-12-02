@file:OptIn(ExperimentalSharedTransitionApi::class)

package dev.engel.flickrpickr

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import dev.engel.flickrpickr.feature.photos.Photos
import dev.engel.flickrpickr.feature.photos.PhotosScreen

@Composable
fun FlickrApp() {

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalSharedTransitionScope provides this,
        ) {
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = Photos,
                popExitTransition = { scaleOut(targetScale = 0.9f) },
                popEnterTransition = { EnterTransition.None },
            ) {
                composable<Photos> { backStackEntry ->
                    val photos = backStackEntry.toRoute<Photos>()

                    CompositionLocalProvider(
                        LocalAnimatedVisibilityScope provides this,
                    ) {
                        PhotosScreen(photos = photos)
                    }
                }
            }
        }
    }
}

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
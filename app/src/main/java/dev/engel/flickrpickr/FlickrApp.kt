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
import dev.engel.flickrpickr.feature.photos.detail.PhotoDetail
import dev.engel.flickrpickr.feature.photos.detail.PhotoDetailScreen

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
                popExitTransition = { slideOutHorizontally() },
                popEnterTransition = { EnterTransition.None },
            ) {
                composable<Photos> { backStackEntry ->
                    val photos = backStackEntry.toRoute<Photos>()

                    CompositionLocalProvider(
                        LocalAnimatedVisibilityScope provides this,
                    ) {
                        PhotosScreen(
                            photos = photos,
                            onNavigateToPhotoDetail = { photo ->
                                navController.navigate(route = PhotoDetail(photoId = photo.id))
                            }
                        )
                    }
                }

                composable<PhotoDetail>(
                    enterTransition = { slideInHorizontally() },
                    exitTransition = { ExitTransition.None }
                ) { backStackEntry ->
                    val photoDetail = backStackEntry.toRoute<PhotoDetail>()

                    CompositionLocalProvider(
                        LocalAnimatedVisibilityScope provides this,
                    ) {
                        PhotoDetailScreen(
                            photoDetail = photoDetail,
                            onCloseDetails = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
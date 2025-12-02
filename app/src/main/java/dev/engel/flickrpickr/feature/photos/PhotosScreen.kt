package dev.engel.flickrpickr.feature.photos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.engel.flickrpickr.R
import kotlinx.serialization.Serializable

@Serializable
object Photos

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PhotosScreen(
    photos: Photos,
    viewModel: PhotosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyGridState = rememberLazyGridState()

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { viewModel.visibleItemIndexChanged(it) }
    }

    LaunchedEffect(viewModel) { viewModel.loadRecent() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_photos_title)) },
            )
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding)
        ) {
            when (val typedState = uiState) {
                is PhotoUiState.Loading -> Text(text = "Loading...")
                is PhotoUiState.Error -> Text(text = typedState.message)
                is PhotoUiState.Ready -> {
                    PhotosReady(typedState, lazyGridState)
                }
            }
        }
    }
}

@Composable
fun PhotosReady(uiState: PhotoUiState.Ready, lazyGridState: LazyGridState) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = lazyGridState
    ) {
        items(
            items = uiState.photos,
            key = { it.id }
        ) { photo ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = photo.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.align(Alignment.Center)
                        .aspectRatio(1f)
                )
            }
        }
    }
}
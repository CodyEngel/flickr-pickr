package dev.engel.flickrpickr.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
                is PhotoUiState.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        state = lazyGridState
                    ) {
                        items(
                            items = typedState.photos
                        ) { photo ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(4.dp)
                            ) {
                                Text(text = photo.title)
                            }
                        }
                    }
                }
            }
        }
    }
}
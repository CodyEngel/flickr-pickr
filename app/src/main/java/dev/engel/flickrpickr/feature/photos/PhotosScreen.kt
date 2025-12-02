package dev.engel.flickrpickr.feature.photos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
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
    val appBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val searchTextFieldState = rememberTextFieldState()

    val focusManager = LocalFocusManager.current

    var searchVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(lazyGridState) {
        snapshotFlow { lazyGridState.layoutInfo.visibleItemsInfo }
            .collect { viewModel.visibleItemsInfoChanged(it) }
    }

    LaunchedEffect(viewModel) { viewModel.loadRecent() }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.screen_photos_title)) },
                scrollBehavior = appBarScrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { innerPadding ->
        // Content
        Column(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .semantics { isTraversalGroup = true },
        ) {
            when (val typedState = uiState) {
                is PhotoUiState.Loading -> Text(text = "Loading...")
                is PhotoUiState.Error -> Text(text = typedState.message)
                is PhotoUiState.Ready -> {
                    PhotosReady(
                        uiState = typedState,
                        lazyGridState = lazyGridState,
                        scrollConnection = appBarScrollBehavior.nestedScrollConnection
                    )
                }
            }
        }

        // Search Overlay
        if (searchVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .safeContentPadding()
            ) {
                DockedSearchBar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Transparent)
                        .semantics { traversalIndex = 0f },
                    inputField = {
                        SearchBarDefaults.InputField(
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    modifier = Modifier.clickable() {
                                        searchTextFieldState.clearText()
                                        focusManager.clearFocus(force = true)
                                    }
                                )
                            },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                            query = searchTextFieldState.text.toString(),
                            onQueryChange = { query ->
                                searchTextFieldState.edit { replace(0, length, query) }
                            },
                            onSearch = { query ->
                                viewModel.search(query)
                                focusManager.clearFocus(force = true)
                            },
                            expanded = false,
                            onExpandedChange = {},
                            placeholder = { Text("Search") },
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    shadowElevation = 16.dp,
                    content = {}
                )
            }
        }
    }
}

@Composable
fun PhotosReady(uiState: PhotoUiState.Ready, lazyGridState: LazyGridState, scrollConnection: NestedScrollConnection) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        state = lazyGridState,
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection),
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
                    modifier = Modifier
                        .align(Alignment.Center)
                        .aspectRatio(1f)
                )
            }
        }
    }
}
package dev.engel.flickrpickr.feature.photos.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import dev.engel.flickrpickr.core.ui.component.SkeletonBox
import kotlinx.serialization.Serializable

@Serializable
data class PhotoDetail(
    val photoId: String
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PhotoDetailScreen(
    photoDetail: PhotoDetail,
    onCloseDetails: () -> Unit,
    viewModel: PhotoDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.loadPhoto(photoDetail.photoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = uiState?.photo?.title ?: "Loading..."
                    Text(text = title)
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onCloseDetails() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val typedState = uiState ?: return@Column
            val photo = typedState.photo
            val exif = typedState.exif
            val info = typedState.info

            SubcomposeAsyncImage(
                model = photo.imageUrl,
                contentDescription = photo.title,
                contentScale = ContentScale.FillWidth,
                loading = { SkeletonBox() },
                modifier = Modifier.fillMaxWidth()
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(16.dp)
            ) {
                // Author
                info?.owner?.let { owner ->
                    val formattedName = owner.realName.ifBlank { owner.username }
                    Text(text = "Credit: $formattedName")
                }

                // Tags
                info?.tags?.tag?.let { tags ->
                    if (!tags.isEmpty()) {
                        Text(text = "Tags")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(tags) { tag ->
                                SuggestionChip(
                                    label = { Text(text = tag.raw) },
                                    onClick = {},
                                )
                            }
                        }
                    }
                }
            }

        }
    }
}
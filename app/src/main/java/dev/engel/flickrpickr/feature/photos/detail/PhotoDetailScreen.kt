package dev.engel.flickrpickr.feature.photos.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = photoDetail.photoId) },
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
        // nothing
    }
}
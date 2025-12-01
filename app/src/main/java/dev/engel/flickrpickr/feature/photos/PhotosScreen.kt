package dev.engel.flickrpickr.feature.photos

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.engel.flickrpickr.R
import kotlinx.serialization.Serializable

@Serializable
object Photos

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PhotosScreen(photos: Photos) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_photos_title)) },
            )
        },
        contentWindowInsets = WindowInsets.safeContent,
    ) { innerPadding ->
        Row(
            modifier = Modifier.padding(innerPadding),
        ) {
            Text(text = "Hello World!")
        }
    }
}
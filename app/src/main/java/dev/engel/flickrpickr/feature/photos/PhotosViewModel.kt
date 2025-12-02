package dev.engel.flickrpickr.feature.photos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val photosRepository: PhotosRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoUiState>(PhotoUiState.Loading)
    val uiState: StateFlow<PhotoUiState> = _uiState

    private val photos = mutableSetOf<Photo>()
    private var nextRequest: PhotosRequest? = null
    private val loadingMutex = Mutex()

    fun loadRecent() {
        _uiState.update { PhotoUiState.Loading }
        photos.clear()

        retrievePhotos(PhotosRequest.Recent())
    }

    fun visibleItemIndexChanged(index: Int) {
        // TODO: this is somewhat clunky since we don't know how many photos are currently visible. This makes infinite
        // scrolling less precise and clunky since the offset isn't always enough to load more items in time.
        // Two things:
        // 1. Let the viewmodel manipulate the List<LazyGridItemInfo> for finer grained information about the current list state.
        // 2. Include a skeleton loading state for "isLoading" which can show up to let the user know more items are loading.
        if (index >= photos.size - 10) {
            loadNext()
        }
    }

    private fun retrievePhotos(request: PhotosRequest) {
        viewModelScope.launch {
            // Using `tryLock` instead of `withLock` to ensure a request is only made once. Using `withLock` along with
            // `isLocked` has the potential to create duplicate requests.
            if (!loadingMutex.tryLock()) return@launch

            try {
                if (photos.isNotEmpty()) {
                    _uiState.update { PhotoUiState.Ready(photos = photos.toList(), isLoadingMore = true) }
                }

                val response = photosRepository.retrieve(request)
                nextRequest = response.nextRequest
                photos += response.photos

                _uiState.update { PhotoUiState.Ready(photos = photos.toList()) }
            } finally {
                loadingMutex.unlock()
            }
        }
    }

    private fun loadNext() {
        nextRequest?.let { request ->
            retrievePhotos(request)
        }
    }
}

sealed class PhotoUiState {
    object Loading : PhotoUiState()
    data class Ready(
        val photos: List<Photo> = emptyList(),
        val isLoadingMore: Boolean = false,
    ) : PhotoUiState()

    data class Error(val message: String) : PhotoUiState()
}
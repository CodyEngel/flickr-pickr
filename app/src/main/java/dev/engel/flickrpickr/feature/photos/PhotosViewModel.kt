package dev.engel.flickrpickr.feature.photos

import android.util.Log
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
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

    fun loadRecent(forceRefresh: Boolean = false) {
        if (!forceRefresh && photos.isNotEmpty()) return

        _uiState.update { PhotoUiState.Loading }
        nextRequest = null
        photos.clear()

        retrievePhotos(PhotosRequest.Recent())
    }

    fun search(query: String) {
        _uiState.update { PhotoUiState.Loading }
        nextRequest = null
        photos.clear()

        retrievePhotos(PhotosRequest.Search(query = query))
    }

    fun visibleItemsInfoChanged(visibleItemsInfo: List<LazyGridItemInfo>) {
        val itemsPerPage = visibleItemsInfo.size
        val lastVisibleItemIndex = visibleItemsInfo.lastOrNull()?.index?.plus(1) ?: 0

        if (lastVisibleItemIndex >= photos.size - (itemsPerPage * 1.5)) {
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
            } catch (e: Exception) {
                Log.e("PhotosViewModel", "Error retrieving photos: ${e.message}", e)
                if (photos.isEmpty()) {
                    _uiState.update { PhotoUiState.Error(message = "Error retrieving photos, please try again later.") }
                } else {
                    _uiState.update { PhotoUiState.Ready(photos = photos.toList()) }
                }
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
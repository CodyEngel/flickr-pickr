package dev.engel.flickrpickr.feature.photos.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoDetailViewModel @Inject constructor(
    private val photoDetailRepository: PhotoDetailRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PhotoDetails?>(null)
    val uiState: StateFlow<PhotoDetails?> = _uiState

    fun loadPhoto(photoId: String) {
        viewModelScope.launch {
            photoDetailRepository.retrieve(photoId).collect { photo ->
                _uiState.update { photo }
            }
        }
    }
}
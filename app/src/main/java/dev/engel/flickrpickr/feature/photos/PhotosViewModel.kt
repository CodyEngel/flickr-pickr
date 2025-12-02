package dev.engel.flickrpickr.feature.photos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.engel.flickrpickr.core.data.network.FlickrApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel @Inject constructor(
    private val flickrApi: FlickrApi
) : ViewModel() {
    init {
        viewModelScope.launch {
            val response =  flickrApi.getRecentPhotos()
            Log.d("PhotosViewModel", response.toString())
        }
    }
}
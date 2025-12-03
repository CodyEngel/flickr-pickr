package dev.engel.flickrpickr.feature.photos.detail

import dev.engel.flickrpickr.feature.photos.Photo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoDetailCache @Inject constructor() {

    private val photosByPhotoId = mutableMapOf<String, Photo>()

    fun add(photo: Photo) {
        photosByPhotoId[photo.id] = photo
    }

    fun get(photoId: String): Photo? {
        return photosByPhotoId[photoId]
    }

    fun clear() {
        photosByPhotoId.clear()
    }
}
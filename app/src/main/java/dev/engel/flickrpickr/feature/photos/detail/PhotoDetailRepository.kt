package dev.engel.flickrpickr.feature.photos.detail

import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import dev.engel.flickrpickr.core.coroutines.IODispatcher
import dev.engel.flickrpickr.core.data.network.FlickrApi
import dev.engel.flickrpickr.core.data.network.FlickrPhotoExif
import dev.engel.flickrpickr.core.data.network.FlickrPhotoInfo
import dev.engel.flickrpickr.feature.photos.Photo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@ViewModelScoped
class PhotoDetailRepository @Inject constructor(
    private val flickrApi: FlickrApi,
    private val photoDetailCache: PhotoDetailCache,
    @param:IODispatcher private val networkDispatcher: CoroutineDispatcher,
) {

    fun retrieve(photoId: String): Flow<PhotoDetails?> {
        return flow {
            val photo = photoDetailCache.get(photoId) ?: return@flow emit(null)

            emit(PhotoDetails(photo))

            val photoResponse = photo.photoResponse
            val photoId = photoResponse.id
            val photoSecret = photoResponse.secret

            val completeDetails = withContext(networkDispatcher) {
                val exif = async {
                    try {
                        flickrApi.getPhotoExif(photoId, photoSecret)
                    } catch (e: Exception) {
                        Log.w("PhotoDetailRepository", "Error retrieving photo exif: ${e.message}", e)
                        null
                    }
                }
                val info = async {
                    try {
                        flickrApi.getPhotoInfo(photoId, photoSecret)
                    } catch (e: Exception) {
                        Log.w("PhotoDetailRepository", "Error retrieving photo info: ${e.message}", e)
                        null
                    }
                }

                PhotoDetails(photo, exif.await()?.photo, info.await()?.photo)
            }

            emit(completeDetails)
        }
    }
}

data class PhotoDetails(
    val photo: Photo,
    val exif: FlickrPhotoExif? = null,
    val info: FlickrPhotoInfo? = null,
)
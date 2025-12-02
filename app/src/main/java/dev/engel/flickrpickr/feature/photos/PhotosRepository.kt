package dev.engel.flickrpickr.feature.photos

import dagger.Reusable
import dev.engel.flickrpickr.core.data.network.FlickrApi
import dev.engel.flickrpickr.core.data.network.FlickrPhotosResponse
import javax.inject.Inject

@Reusable
class PhotosRepository @Inject constructor(
    private val flickrApi: FlickrApi
) {

    suspend fun retrieve(request: PhotosRequest): PhotosResponse {
        return when (request) {
            is PhotosRequest.Recent -> {
                flickrApi.getRecentPhotos(page = request.page, perPage = request.perPage)
            }
            is PhotosRequest.Search -> {
                flickrApi.searchPhotos(query = request.query, page = request.page, perPage = request.perPage)
            }
        }.let { response -> mapResponse(response, request) }
    }

    private fun mapResponse(response: FlickrPhotosResponse, request: PhotosRequest): PhotosResponse {
        return PhotosResponse(
            photos = mapPhotos(response),
            nextRequest = nextRequest(request, response)
        )
    }

    private fun mapPhotos(response: FlickrPhotosResponse): List<Photo> {
        return response.photos.photo.map { photo ->
            Photo(
                id = photo.id,
                imageUrl = "https://live.staticflickr.com/${photo.server}/${photo.id}_${photo.secret}.jpg",
                title = photo.title,
            )
        }
    }

    private fun nextRequest(request: PhotosRequest, response: FlickrPhotosResponse): PhotosRequest? {
        val totalPages = response.photos.pages
        val currentPage = request.page
        if (totalPages <= currentPage) return null

        return request.nextPage()
    }
}

sealed class PhotosRequest {
    abstract val page: Int
    abstract val perPage: Int

    abstract fun nextPage(): PhotosRequest

    data class Recent(
        override val page: Int = 1,
        override val perPage: Int = 30
    ) : PhotosRequest() {
        override fun nextPage(): PhotosRequest {
            return copy(page = page + 1)
        }

    }

    data class Search(
        val query: String,
        override val page: Int,
        override val perPage: Int,
    ) : PhotosRequest() {
        override fun nextPage(): PhotosRequest {
            return copy(page = page + 1)
        }
    }
}

data class PhotosResponse(
    val photos: List<Photo>,
    val nextRequest: PhotosRequest?,
)

data class Photo(
    val id: String,
    val imageUrl: String,
    val title: String,
)
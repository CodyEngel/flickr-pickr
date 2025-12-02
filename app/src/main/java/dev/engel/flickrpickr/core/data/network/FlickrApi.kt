package dev.engel.flickrpickr.core.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET("rest/?method=flickr.photos.getRecent")
    suspend fun getRecentPhotos(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): FlickrPhotosResponse

    @GET("rest/?method=flickr.photos.search")
    suspend fun searchPhotos(
        @Query("text") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): FlickrPhotosResponse
}

@Serializable
data class FlickrPhotosResponse(
    val photos: FlickrPhotosPage,
    val stat: String
)

@Serializable
data class FlickrPhotosPage(
    val page: Int,
    val pages: Int,
    @SerialName("perpage") val perPage: Int,
    val total: Int,
    val photo: List<FlickrPhoto>
)

@Serializable
data class FlickrPhoto(
    val id: String,
    val owner: String,
    val secret: String,
    val server: String,
    val farm: Int,
    val title: String
) {
    val imageUrl: String
        get() = "https://live.staticflickr.com/$server/${id}_$secret.jpg"
}
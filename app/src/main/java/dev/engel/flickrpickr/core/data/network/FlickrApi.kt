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

    @GET("rest/?method=flickr.photos.getExif")
    suspend fun getPhotoExif(
        @Query("photo_id") photoId: String,
        @Query("secret") secret: String,
    ): FlickrPhotoExifResponse

    @GET("rest/?method=flickr.photos.getInfo")
    suspend fun getPhotoInfo(
        @Query("photo_id") photoId: String,
        @Query("secret") secret: String,
    ): FlickrPhotoInfoResponse
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
)

@Serializable
data class FlickrPhotoExifResponse(
    val photo: FlickrPhotoExif
)

@Serializable
data class FlickrPhotoExif(
    val camera: String,
    val exif: List<FlickrExifData>
)

@Serializable
data class FlickrExifData(
    val tag: String,
    val label: String,
    val raw: FlickrContent,
    val clean: FlickrContent? = null
)

@Serializable
data class FlickrPhotoInfoResponse(
    val photo: FlickrPhotoInfo
)

@Serializable
data class FlickrPhotoInfo(
    val owner: FlickrUser,
    val title: FlickrContent,
    val description: FlickrContent,
    val dates: FlickrPhotoDates,
    val views: String,
    val comments: FlickrContent,
    val tags: FlickrTags
)

@Serializable
data class FlickrUser(
    val username: String,
    @SerialName("realname") val realName: String,
    val location: String?,
)

@Serializable
data class FlickrPhotoDates(
    val posted: String,
    val taken: String
)

@Serializable
data class FlickrTags(
    val tag: List<FlickrPhotoTag>
)

@Serializable
data class FlickrPhotoTag(
    val id: String,
    val author: String,
    @SerialName("authorname") val authorName: String,
    val raw: String,
    @SerialName("_content") val content: String
)

@Serializable
data class FlickrContent(
    @SerialName("_content") val content: String
)
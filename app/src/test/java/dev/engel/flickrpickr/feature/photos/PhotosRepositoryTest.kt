package dev.engel.flickrpickr.feature.photos

import dev.engel.flickrpickr.core.data.network.FlickrApi
import dev.engel.flickrpickr.core.data.network.FlickrPhoto
import dev.engel.flickrpickr.core.data.network.FlickrPhotosPage
import dev.engel.flickrpickr.core.data.network.FlickrPhotosResponse
import dev.engel.flickrpickr.feature.photos.detail.PhotoDetailCache
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockFlickrApi = mockk<FlickrApi>()
    private val mockPhotoDetailCache = mockk<PhotoDetailCache>(relaxed = true)
    private lateinit var subject: PhotosRepository

    @BeforeEach
    fun setUp() {
        subject = PhotosRepository(
            flickrApi = mockFlickrApi,
            photoDetailCache = mockPhotoDetailCache,
            networkDispatcher = testDispatcher
        )
    }

    @Nested
    inner class RetrieveRecent {
        @Test
        fun `calls getRecentPhotos with correct parameters`() = runTest(testDispatcher) {
            val request = PhotosRequest.Recent(page = 2, perPage = 30)
            coEvery { mockFlickrApi.getRecentPhotos(page = 2, perPage = 30) } returns createFlickrResponse()

            subject.retrieve(request)

            coVerify { mockFlickrApi.getRecentPhotos(page = 2, perPage = 30) }
        }

        @Test
        fun `returns mapped photos from API response`() = runTest(testDispatcher) {
            val flickrPhotos = createFlickrPhotos(3)
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = flickrPhotos)

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos).hasSize(3)
        }

        @Test
        fun `returns empty list when API returns no photos`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = emptyList())

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos).hasSize(0)
        }
    }

    @Nested
    inner class RetrieveSearch {
        @Test
        fun `calls searchPhotos with correct parameters`() = runTest(testDispatcher) {
            val request = PhotosRequest.Search(query = "sunset", page = 3, perPage = 50)
            coEvery { mockFlickrApi.searchPhotos(query = "sunset", page = 3, perPage = 50) } returns createFlickrResponse()

            subject.retrieve(request)

            coVerify { mockFlickrApi.searchPhotos(query = "sunset", page = 3, perPage = 50) }
        }

        @Test
        fun `returns mapped photos from search response`() = runTest(testDispatcher) {
            val flickrPhotos = createFlickrPhotos(5)
            coEvery { mockFlickrApi.searchPhotos(any(), any(), any()) } returns createFlickrResponse(photos = flickrPhotos)

            val result = subject.retrieve(PhotosRequest.Search(query = "cats"))

            expectThat(result.photos).hasSize(5)
        }
    }

    @Nested
    inner class PhotoMapping {
        @Test
        fun `maps photo id correctly`() = runTest(testDispatcher) {
            val flickrPhoto = createFlickrPhoto(id = "12345")
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = listOf(flickrPhoto))

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos.first().id).isEqualTo("12345")
        }

        @Test
        fun `maps photo title correctly`() = runTest(testDispatcher) {
            val flickrPhoto = createFlickrPhoto(title = "Beautiful Sunset")
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = listOf(flickrPhoto))

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos.first().title).isEqualTo("Beautiful Sunset")
        }

        @Test
        fun `constructs image URL from server, id, and secret`() = runTest(testDispatcher) {
            val flickrPhoto = createFlickrPhoto(
                id = "photo123",
                server = "server456",
                secret = "secret789"
            )
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = listOf(flickrPhoto))

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos.first().imageUrl)
                .isEqualTo("https://live.staticflickr.com/server456/photo123_secret789.jpg")
        }

        @Test
        fun `preserves original FlickrPhoto in photoResponse`() = runTest(testDispatcher) {
            val flickrPhoto = createFlickrPhoto(id = "test123")
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = listOf(flickrPhoto))

            val result = subject.retrieve(PhotosRequest.Recent())

            expectThat(result.photos.first().photoResponse).isEqualTo(flickrPhoto)
        }
    }

    @Nested
    inner class Pagination {
        @Test
        fun `returns nextRequest when more pages available`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(
                currentPage = 1,
                totalPages = 5
            )

            val result = subject.retrieve(PhotosRequest.Recent(page = 1))

            expectThat(result.nextRequest).isA<PhotosRequest.Recent>()
                .get { page }.isEqualTo(2)
        }

        @Test
        fun `returns null nextRequest when on last page`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(
                currentPage = 5,
                totalPages = 5
            )

            val result = subject.retrieve(PhotosRequest.Recent(page = 5))

            expectThat(result.nextRequest).isNull()
        }

        @Test
        fun `returns null nextRequest when only one page exists`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(
                currentPage = 1,
                totalPages = 1
            )

            val result = subject.retrieve(PhotosRequest.Recent(page = 1))

            expectThat(result.nextRequest).isNull()
        }

        @Test
        fun `preserves search query in nextRequest`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.searchPhotos(any(), any(), any()) } returns createFlickrResponse(
                currentPage = 1,
                totalPages = 3
            )

            val result = subject.retrieve(PhotosRequest.Search(query = "mountains", page = 1))

            expectThat(result.nextRequest).isA<PhotosRequest.Search>().and {
                get { query }.isEqualTo("mountains")
                get { page }.isEqualTo(2)
            }
        }

        @Test
        fun `preserves perPage in nextRequest for Recent`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(
                currentPage = 1,
                totalPages = 3
            )

            val result = subject.retrieve(PhotosRequest.Recent(page = 1, perPage = 25))

            expectThat(result.nextRequest).isA<PhotosRequest.Recent>()
                .get { perPage }.isEqualTo(25)
        }

        @Test
        fun `preserves perPage in nextRequest for Search`() = runTest(testDispatcher) {
            coEvery { mockFlickrApi.searchPhotos(any(), any(), any()) } returns createFlickrResponse(
                currentPage = 1,
                totalPages = 3
            )

            val result = subject.retrieve(PhotosRequest.Search(query = "test", page = 1, perPage = 100))

            expectThat(result.nextRequest).isA<PhotosRequest.Search>()
                .get { perPage }.isEqualTo(100)
        }
    }

    @Nested
    inner class PhotoDetailCacheIntegration {
        @Test
        fun `adds each photo to cache`() = runTest(testDispatcher) {
            val flickrPhotos = createFlickrPhotos(3)
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = flickrPhotos)

            subject.retrieve(PhotosRequest.Recent())

            verify(exactly = 3) { mockPhotoDetailCache.add(any()) }
        }

        @Test
        fun `adds correct photo to cache`() = runTest(testDispatcher) {
            val flickrPhoto = createFlickrPhoto(id = "cached123", title = "Cached Photo")
            coEvery { mockFlickrApi.getRecentPhotos(any(), any()) } returns createFlickrResponse(photos = listOf(flickrPhoto))

            subject.retrieve(PhotosRequest.Recent())

            verify {
                mockPhotoDetailCache.add(match { photo ->
                    photo.id == "cached123" && photo.title == "Cached Photo"
                })
            }
        }
    }

    private fun createFlickrPhoto(
        id: String = "1",
        owner: String = "owner_$id",
        secret: String = "secret_$id",
        server: String = "server_$id",
        farm: Int = 1,
        title: String = "Photo $id"
    ): FlickrPhoto {
        return FlickrPhoto(
            id = id,
            owner = owner,
            secret = secret,
            server = server,
            farm = farm,
            title = title
        )
    }

    private fun createFlickrPhotos(count: Int, idPrefix: String = "photo"): List<FlickrPhoto> {
        return (1..count).map { createFlickrPhoto(id = "${idPrefix}_$it") }
    }

    private fun createFlickrResponse(
        photos: List<FlickrPhoto> = emptyList(),
        currentPage: Int = 1,
        totalPages: Int = 1,
        perPage: Int = 60,
        total: Int = photos.size
    ): FlickrPhotosResponse {
        return FlickrPhotosResponse(
            photos = FlickrPhotosPage(
                page = currentPage,
                pages = totalPages,
                perPage = perPage,
                total = total,
                photo = photos
            ),
            stat = "ok"
        )
    }
}

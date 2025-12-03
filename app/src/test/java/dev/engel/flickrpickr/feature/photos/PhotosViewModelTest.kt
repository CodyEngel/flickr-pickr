package dev.engel.flickrpickr.feature.photos

import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import dev.engel.flickrpickr.core.data.network.FlickrPhoto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val mockPhotosRepository = mockk<PhotosRepository>()
    private lateinit var subject: PhotosViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        subject = PhotosViewModel(mockPhotosRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState should be Loading`() {
        expectThat(subject.uiState.value).isEqualTo(PhotoUiState.Loading)
    }

    @Nested
    inner class LoadRecent {
        @Test
        fun `returns empty list when repository returns no photos`() = runTest(testDispatcher) {
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(emptyList(), null)

            waitUntilIdle { subject.loadRecent() }

            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = emptyList(), isLoadingMore = false)
            )
        }

        @Test
        fun `returns photos when repository returns photos`() = runTest(testDispatcher) {
            val photos = createPhotos(3)
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(photos, null)

            waitUntilIdle { subject.loadRecent() }

            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = photos, isLoadingMore = false)
            )
        }

        @Test
        fun `does not reload when photos already exist`() = runTest(testDispatcher) {
            val initialPhotos = createPhotos(3)
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(initialPhotos, null)
            waitUntilIdle { subject.loadRecent() }

            // Call loadRecent again
            waitUntilIdle { subject.loadRecent() }

            // Repository should only be called once
            coVerify(exactly = 1) { mockPhotosRepository.retrieve(PhotosRequest.Recent()) }
        }

        @Test
        fun `shows error state when repository throws and no photos loaded`() = runTest(testDispatcher) {
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } throws RuntimeException("Network error")

            waitUntilIdle { subject.loadRecent() }

            expectThat(subject.uiState.value).isA<PhotoUiState.Error>()
                .get { message }.isEqualTo("Error retrieving photos, please try again later.")
        }
    }

    @Nested
    inner class Search {
        @Test
        fun `returns photos matching search query`() = runTest(testDispatcher) {
            val query = "sunset"
            val photos = createPhotos(5)
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Search(query = query)) } returns PhotosResponse(photos, null)

            waitUntilIdle { subject.search(query) }

            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = photos, isLoadingMore = false)
            )
        }

        @Test
        fun `clears previous photos when searching`() = runTest(testDispatcher) {
            val recentPhotos = createPhotos(3, idPrefix = "recent")
            val searchPhotos = createPhotos(2, idPrefix = "search")
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(recentPhotos, null)
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Search(query = "cats")) } returns PhotosResponse(searchPhotos, null)

            waitUntilIdle { subject.loadRecent() }
            waitUntilIdle { subject.search("cats") }

            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = searchPhotos, isLoadingMore = false)
            )
        }

        @Test
        fun `shows error state when search fails and no photos loaded`() = runTest(testDispatcher) {
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Search(query = "test")) } throws RuntimeException("Network error")

            waitUntilIdle { subject.search("test") }

            expectThat(subject.uiState.value).isA<PhotoUiState.Error>()
        }
    }

    @Nested
    inner class Pagination {
        @Test
        fun `loads next page when scrolled near end`() = runTest(testDispatcher) {
            val page1Photos = createPhotos(10, idPrefix = "page1")
            val page2Photos = createPhotos(10, idPrefix = "page2")
            val nextRequest = PhotosRequest.Recent(page = 2)

            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(page1Photos, nextRequest)
            coEvery { mockPhotosRepository.retrieve(nextRequest) } returns PhotosResponse(page2Photos, null)

            waitUntilIdle { subject.loadRecent() }

            // Simulate scrolling to near the end (last visible item is at index 9, with 10 items per page)
            val visibleItems = createVisibleItemsInfo(startIndex = 5)
            waitUntilIdle { subject.visibleItemsInfoChanged(visibleItems) }

            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = page1Photos + page2Photos, isLoadingMore = false)
            )
        }

        @Test
        fun `does not load next page when not near end`() = runTest(testDispatcher) {
            val photos = createPhotos(100, idPrefix = "page1")
            val nextRequest = PhotosRequest.Recent(page = 2)

            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(photos, nextRequest)

            waitUntilIdle { subject.loadRecent() }

            // Simulate scrolling but not near end (viewing items 0-9 of 100)
            val visibleItems = createVisibleItemsInfo(startIndex = 0)
            waitUntilIdle { subject.visibleItemsInfoChanged(visibleItems) }

            // Should only call retrieve once (initial load)
            coVerify(exactly = 1) { mockPhotosRepository.retrieve(any()) }
        }

        @Test
        fun `does not load next page when no next request available`() = runTest(testDispatcher) {
            val photos = createPhotos(10)
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(photos, null)

            waitUntilIdle { subject.loadRecent() }

            // Simulate scrolling to end
            val visibleItems = createVisibleItemsInfo(startIndex = 5)
            waitUntilIdle { subject.visibleItemsInfoChanged(visibleItems) }

            // Should only call retrieve once (initial load)
            coVerify(exactly = 1) { mockPhotosRepository.retrieve(any()) }
        }

        @Test
        fun `shows isLoadingMore true while loading next page`() = runTest(testDispatcher) {
            val page1Photos = createPhotos(10, idPrefix = "page1")
            val nextRequest = PhotosRequest.Recent(page = 2)
            var capturedLoadingState: PhotoUiState? = null

            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(page1Photos, nextRequest)
            coEvery { mockPhotosRepository.retrieve(nextRequest) } coAnswers {
                // Capture state during loading (before response returns)
                capturedLoadingState = subject.uiState.value
                PhotosResponse(emptyList(), null)
            }

            waitUntilIdle { subject.loadRecent() }

            val visibleItems = createVisibleItemsInfo(startIndex = 5)
            waitUntilIdle { subject.visibleItemsInfoChanged(visibleItems) }

            // Verify the state was isLoadingMore=true during the network call
            expectThat(capturedLoadingState).isA<PhotoUiState.Ready>()
                .get { isLoadingMore }.isEqualTo(true)
        }
    }

    @Nested
    inner class ErrorHandling {
        @Test
        fun `shows error state when initial load fails with no existing photos`() = runTest(testDispatcher) {
            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } throws RuntimeException("Network error")

            waitUntilIdle { subject.loadRecent() }

            expectThat(subject.uiState.value).isA<PhotoUiState.Error>()
                .get { message }.isEqualTo("Error retrieving photos, please try again later.")
        }

        @Test
        fun `preserves existing photos when error occurs during pagination`() = runTest(testDispatcher) {
            val page1Photos = createPhotos(10)
            val nextRequest = PhotosRequest.Recent(page = 2)

            coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(page1Photos, nextRequest)
            coEvery { mockPhotosRepository.retrieve(nextRequest) } throws RuntimeException("Network error")

            waitUntilIdle { subject.loadRecent() }

            val visibleItems = createVisibleItemsInfo(startIndex = 5)
            waitUntilIdle { subject.visibleItemsInfoChanged(visibleItems) }

            // Should still show existing photos, not error state
            expectThat(subject.uiState.value).isEqualTo(
                PhotoUiState.Ready(photos = page1Photos, isLoadingMore = false)
            )
        }
    }

    private fun TestScope.waitUntilIdle(block: () -> Unit) {
        block()
        advanceUntilIdle()
    }


    private fun createPhoto(id: String = "1"): Photo {
        return Photo(
            id = id,
            imageUrl = "https://example.com/$id.jpg",
            title = "Photo $id",
            photoResponse = createFlickrPhoto(id)
        )
    }

    private fun createPhotos(count: Int, idPrefix: String = "photo"): List<Photo> {
        return (1..count).map { createPhoto(id = "${idPrefix}_$it") }
    }

    private fun createFlickrPhoto(id: String = "1"): FlickrPhoto {
        return FlickrPhoto(
            id = id,
            owner = "owner_$id",
            secret = "secret_$id",
            server = "server_$id",
            farm = 1,
            title = "Photo $id"
        )
    }

    private fun createVisibleItemsInfo(startIndex: Int, count: Int = 10): List<LazyGridItemInfo> {
        return (startIndex until startIndex + count).map { index ->
            mockk<LazyGridItemInfo> {
                every { this@mockk.index } returns index
            }
        }
    }
}
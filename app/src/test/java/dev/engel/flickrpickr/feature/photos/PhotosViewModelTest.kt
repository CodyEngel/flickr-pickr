package dev.engel.flickrpickr.feature.photos

import io.mockk.coEvery
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
import org.junit.jupiter.api.Test
import strikt.api.expectThat
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
    fun `given a subject that has just launched, uiState should be Loading`() {
        expectThat(subject.uiState.value).isEqualTo(PhotoUiState.Loading)
    }

    @Test
    fun `given a subject that is retrieving recent photos, uiState should reflect newly loaded photos`() = runTest(testDispatcher) {
        coEvery { mockPhotosRepository.retrieve(PhotosRequest.Recent()) } returns PhotosResponse(emptyList(), null)

        waitUntilIdle { subject.loadRecent() }

        expectThat(subject.uiState.value).isEqualTo(
            PhotoUiState.Ready(
                photos = emptyList(),
                isLoadingMore = false
            )
        )
    }

    private fun TestScope.waitUntilIdle(block: () -> Unit) {
        block()
        advanceUntilIdle()
    }
}
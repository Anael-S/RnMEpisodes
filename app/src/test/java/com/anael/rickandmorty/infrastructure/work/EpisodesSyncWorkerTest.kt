package com.anael.rickandmorty.infrastructure.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.anael.rickandmorty.data.repository.EpisodesRepositoryImpl
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EpisodesSyncWorkerTest {

    private lateinit var context: Context
    private lateinit var repository: EpisodesRepositoryImpl

    @Before
    fun setUp() {
        // This needs Robolectric runner to work in a unit test
        context = ApplicationProvider.getApplicationContext()
        repository = mockk(relaxed = true)
    }

    @Test
    fun `worker calls repository syncEpisodes and returns success`() = runTest {
        coEvery { repository.syncEpisodes() } returns Result.success(Unit)

        val worker = TestListenableWorkerBuilder<EpisodesSyncWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    params: WorkerParameters
                ): ListenableWorker {
                    return EpisodesSyncWorker(appContext, params, repository)
                }
            })
            .build()

        val result = worker.doWork()

        coVerify(exactly = 1) { repository.syncEpisodes() }
        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
    }

    @Test
    fun `worker returns RETRY for network-like failure`() = runTest {
        // Arrange
        coEvery { repository.syncEpisodes() } returns Result.failure(IOException("boom"))

        val worker = TestListenableWorkerBuilder<EpisodesSyncWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    params: WorkerParameters
                ): ListenableWorker = EpisodesSyncWorker(appContext, params, repository)
            })
            .build()

        // Act
        val result = worker.doWork()

        // Assert
        coVerify(exactly = 1) { repository.syncEpisodes() }
        assertThat(result).isInstanceOf(ListenableWorker.Result.Retry::class.java)
    }
}

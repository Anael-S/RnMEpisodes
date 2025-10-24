package com.anael.rickandmorty.infrastructure.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker that sync the Episodes (fetches them from the API > write them in DB)
 * Also update the last refreshed timestamp in DB
 */
@HiltWorker
class EpisodesSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: EpisodesRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val result = repository.syncEpisodes()
            if (result.isSuccess) {
                // background refresh succeeded -> update timestamp in the DB
                repository.markLastRefreshNow()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (t: Throwable) {
            Result.retry()
        }
    }
}

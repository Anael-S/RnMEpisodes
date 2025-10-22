package com.anael.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.local.LastRefreshEntity
import com.anael.rickandmorty.data.mapper.toDomain
import com.anael.rickandmorty.data.paging.EpisodesRemoteMediatorFactory
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import com.anael.rickandmorty.data.utils.safeCall
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


@OptIn(ExperimentalPagingApi::class)
class EpisodesRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
    private val mediatorFactory: EpisodesRemoteMediatorFactory,
    private val remote: RnMApiRemoteDataSource
) : EpisodesRepository {

    override val lastRefreshFlow: Flow<Long?> = db.lastRefreshDao().getTimestampFlow()

    override fun getEpisodesStream(): Flow<PagingData<Episode>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 60,
                enablePlaceholders = false
            ),
            remoteMediator = mediatorFactory.create(),
            pagingSourceFactory = { db.episodeDao().pagingSource() }
        ).flow.map { paging -> paging.map { it.toDomain() } }
    }

    override suspend fun getEpisodeDetail(episodeId: String): Result<Episode> =
        safeCall { remote.getEpisodeById(episodeId) }.map { it.toDomain() }

    override suspend fun syncEpisodes(): Result<Unit> = runCatching {
        // If this was to invalidate the PagingSource, prefer calling refresh() from UI.
        db.withTransaction {
            db.episodeRemoteKeyDao().clearRemoteKeys()
            db.episodeDao().clearAll()
        }
    }

    override suspend fun markLastRefreshNow() {
        db.lastRefreshDao().upsert(LastRefreshEntity(timestamp = System.currentTimeMillis()))
    }
}

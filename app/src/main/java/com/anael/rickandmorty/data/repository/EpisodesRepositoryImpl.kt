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
import com.anael.rickandmorty.data.mapper.toEntity
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
        var page = 1
        var reachedEnd = false
        val seenIds = mutableListOf<Int>()

        while (!reachedEnd) {
            val dto = remote.getEpisodesPage(page)
            val items = dto.results
            val entities = items.map { it.toEntity() }

            db.withTransaction {
                // 1) upsert rows
                db.episodeDao().upsertAll(entities)

                // 2) write keys for these rows
                val prev = (page - 1).takeIf { it >= 1 }
                val next = if (dto.info.next == null) null else page + 1
                val keys = entities.map {
                    com.anael.rickandmorty.data.local.EpisodeRemoteKey(
                        episodeId = it.id,
                        prevKey = prev,
                        nextKey = next
                    )
                }
                db.episodeRemoteKeyDao().upsertAll(keys)

                // 3) mark last refresh on the first page
                if (page == 1) {
                    db.lastRefreshDao().upsert(
                        LastRefreshEntity(
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }

            seenIds += entities.map { it.id }
            reachedEnd = dto.info.next == null
            page++
        }
    }


    override suspend fun markLastRefreshNow() {
        db.lastRefreshDao().upsert(LastRefreshEntity(timestamp = System.currentTimeMillis()))
    }
}

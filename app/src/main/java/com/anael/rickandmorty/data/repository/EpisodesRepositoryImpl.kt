package com.anael.rickandmorty.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.room.withTransaction
import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.local.EpisodeEntity
import com.anael.rickandmorty.data.local.EpisodesRemoteMediator
import com.anael.rickandmorty.data.local.LastRefreshEntity
import com.anael.rickandmorty.data.mapper.toDomain
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.data.remote.RnMApiService
import com.anael.rickandmorty.data.utils.safeCall
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class EpisodesRepositoryImpl @Inject constructor(
    private val service: RnMApiService,
    private val db: AppDatabase
) : EpisodesRepository {

    override val lastRefreshFlow: Flow<Long?> = db.lastRefreshDao().getTimestampFlow()

    // UI reads from DB; mediator fills DB from network
    override fun getEpisodesStream(): Flow<PagingData<Episode>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                initialLoadSize = 60,
                enablePlaceholders = false
            ),
            remoteMediator = EpisodesRemoteMediator(service, db),
            pagingSourceFactory = { db.episodeDao().pagingSource() }
        ).flow
            .map { pagingData -> pagingData.map { entity: EpisodeEntity -> entity.toDomain() } }
    }

    // Network detail -> map DTO to Domain
    override suspend fun getEpisodeDetail(episodeId: String): Result<Episode> =
        safeCall { service.getEpisodeById(episodeId) } // Result<EpisodeDto>
            .map { dto: EpisodeDto -> dto.toDomain() }  // Result<Episode>

    override suspend fun syncEpisodes(): Result<Unit> = runCatching {
        db.withTransaction {
            db.episodeRemoteKeyDao().clearRemoteKeys()
            db.episodeDao().insertNoop(NOOP_EPISODE)
            db.episodeDao().deleteById(-1)
        }
    }

    override suspend fun markLastRefreshNow() {
        db.lastRefreshDao().upsert(
            LastRefreshEntity(timestamp = System.currentTimeMillis())
        )
    }
}

private val NOOP_EPISODE = EpisodeEntity(
    id = -1,
    name = "",
    airDate = "",
    episodeCode = "",
    characters = emptyList(),
    url = "",
    created = ""
)

package com.anael.rickandmorty.domain.repository

import androidx.paging.PagingData
import com.anael.rickandmorty.domain.model.Episode
import kotlinx.coroutines.flow.Flow

interface EpisodesRepository {
    val lastRefreshFlow: Flow<Long?>
    fun getEpisodesStream(): Flow<PagingData<Episode>>
    suspend fun getEpisodeDetail(episodeId: String): Result<Episode>
    suspend fun syncEpisodes(): Result<Unit>
    suspend fun markLastRefreshNow()
}

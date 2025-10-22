package com.anael.rickandmorty.data.local

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.anael.rickandmorty.data.mapper.toEntity
import com.anael.rickandmorty.data.remote.RnMApiService

@OptIn(ExperimentalPagingApi::class)
class EpisodesRemoteMediator(
    private val service: RnMApiService,
    private val db: AppDatabase
) : RemoteMediator<Int, EpisodeEntity>() {

    private val episodeDao = db.episodeDao()
    private val keysDao = db.episodeRemoteKeyDao()

    override suspend fun initialize(): InitializeAction =
        InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EpisodeEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                // Resume near the viewport if we can, else start at 1
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
                    // No more items before
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                prevKey
            }

            LoadType.APPEND -> {
                // IMPORTANT: don’t end early if we can’t read items/keys yet.
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = false)

                val remoteKeys = keysDao.remoteKeyById(lastItem.id)
                    ?: return MediatorResult.Success(endOfPaginationReached = false)

                val nextKey = remoteKeys.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = true)

                nextKey
            }
        }

        return try {
            val response = service.getEpisodesPage(page)
            val items = response.results
            val endOfPagination = response.info.next == null || items.isEmpty()
            val entities = items.map { it.toEntity() }

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    keysDao.clearRemoteKeys()
                    episodeDao.clearAll() //We reload ALL episodes from the list if a refresh has been asked
                }

                episodeDao.upsertAll(entities)

                val prev = if (page == 1) null else page - 1
                val next = if (endOfPagination) null else page + 1
                val keys = entities.map {
                    EpisodeRemoteKey(episodeId = it.id, prevKey = prev, nextKey = next)
                }
                keysDao.upsertAll(keys)

                // Update refresh timestamp only after successful REFRESH
                if (loadType == LoadType.REFRESH) {
                    db.lastRefreshDao().upsert(LastRefreshEntity(timestamp = System.currentTimeMillis()))
                }
            }

            MediatorResult.Success(endOfPaginationReached = endOfPagination)
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EpisodeEntity>): EpisodeRemoteKey? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { keysDao.remoteKeyById(it.id) }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EpisodeEntity>): EpisodeRemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { keysDao.remoteKeyById(it.id) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EpisodeEntity>): EpisodeRemoteKey? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestItemToPosition(anchor) ?: return null
        return keysDao.remoteKeyById(closest.id)
    }
}

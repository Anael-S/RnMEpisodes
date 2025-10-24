package com.anael.rickandmorty.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.anael.rickandmorty.data.local.AppDatabase
import com.anael.rickandmorty.data.local.EpisodeEntity
import com.anael.rickandmorty.data.local.EpisodeRemoteKey
import com.anael.rickandmorty.data.local.LastRefreshEntity
import com.anael.rickandmorty.data.mapper.toEntity
import com.anael.rickandmorty.data.remote.RnMApiRemoteDataSource
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

/**
 * Mediator used for the paginated episodes list
 * The paging data is retrieved from the DB (AppDatabase)
 * But updated here in the mediator (and a separate worker)
 */
@OptIn(ExperimentalPagingApi::class)
class EpisodesRemoteMediator @Inject constructor(
    private val remote: RnMApiRemoteDataSource,
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
        // Decide which page to load
        val page = when (loadType) {
            LoadType.REFRESH -> {
                // Resume near viewport if possible, otherwise start at 1
                getRemoteKeyClosestToCurrentPosition(state)?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val prevKey = getRemoteKeyForFirstItem(state)?.prevKey
                if (prevKey == null) {
                    // No items before
                    return MediatorResult.Success(endOfPaginationReached = true)
                }
                prevKey
            }
            LoadType.APPEND -> {
                // Don’t end early if we can’t read items/keys yet
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = false)

                val nextKey = keysDao.remoteKeyById(lastItem.id)?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = false)

                nextKey
            }
        }

        return try {
            // Network
            val dto = remote.getEpisodesPage(page)
            val items = dto.results
            val endOfPagination = dto.info.next == null || items.isEmpty()
            val entities = items.map { it.toEntity() }

            // DB transaction
            db.withTransaction {
                if (loadType == LoadType.REFRESH) { //eg: pullDownRefresh
                    keysDao.clearRemoteKeys()
                    episodeDao.clearAll()
                }

                episodeDao.upsertAll(entities)

                val prev = (page - 1).takeIf { it >= 1 }
                val next = if (endOfPagination) null else page + 1

                val keys = entities.map {
                    EpisodeRemoteKey(
                        episodeId = it.id,
                        prevKey = prev,
                        nextKey = next
                    )
                }
                keysDao.upsertAll(keys)

                if (loadType == LoadType.REFRESH) {
                    db.lastRefreshDao().upsert(
                        LastRefreshEntity(timestamp = System.currentTimeMillis())
                    )
                }
            }

            MediatorResult.Success(endOfPaginationReached = endOfPagination)
        } catch (e: IOException) {
            MediatorResult.Error(e)  // network / I/O
        } catch (e: HttpException) {
            MediatorResult.Error(e)  // non-2xx
        } catch (t: Throwable) {
            MediatorResult.Error(t)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(
        state: PagingState<Int, EpisodeEntity>
    ): EpisodeRemoteKey? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }
            ?.data?.firstOrNull()
            ?.let { keysDao.remoteKeyById(it.id) }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, EpisodeEntity>
    ): EpisodeRemoteKey? {
        val anchor = state.anchorPosition ?: return null
        val closest = state.closestItemToPosition(anchor) ?: return null
        return keysDao.remoteKeyById(closest.id)
    }
}

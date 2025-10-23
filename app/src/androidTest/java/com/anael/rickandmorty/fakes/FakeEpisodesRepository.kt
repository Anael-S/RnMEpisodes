package com.anael.rickandmorty.fakes

import androidx.paging.*
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.domain.repository.EpisodesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.paging.PagingSource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList


@Singleton
class FakeEpisodesRepository @Inject constructor() : EpisodesRepository {

  sealed interface Mode {
    data class Success(val items: List<Episode>, val pageSize: Int = 20) : Mode
    data object FirstLoadError : Mode
    data class AppendError(val failAfter: Int = 40, val pageSize: Int = 20) : Mode
    data class Endless(val total: Int = 60, val pageSize: Int = 20) : Mode
  }
  @Volatile var mode: Mode = Mode.Success(items = demoEpisodes(40))

  private val _lastRefreshFlow = MutableStateFlow<Long?>(null)
  override val lastRefreshFlow: Flow<Long?> = _lastRefreshFlow

  override fun getEpisodesStream(): Flow<PagingData<Episode>> {
    val pageSize = when (val m = mode) {
      is Mode.Success        -> m.pageSize
      is Mode.AppendError    -> m.pageSize
      is Mode.Endless        -> m.pageSize
      is Mode.FirstLoadError -> 20
    }.coerceAtLeast(1)

    return Pager(PagingConfig(pageSize = pageSize, enablePlaceholders = false)) {
      when (val m = mode) {
        is Mode.Success        -> SuccessSource(m.items, m.pageSize)
        is Mode.FirstLoadError -> FirstLoadErrorSource()
        is Mode.AppendError    -> AppendErrorSource(m.failAfter, m.pageSize)
        is Mode.Endless        -> EndlessSource(m.total, m.pageSize)
      }
    }.flow
  }

  override suspend fun getEpisodeDetail(episodeId: String): Result<Episode> {
    val all = when (val m = mode) {
      is Mode.Success     -> m.items
      is Mode.AppendError -> demoEpisodes(m.failAfter)
      is Mode.Endless     -> demoEpisodes(m.total)
      is Mode.FirstLoadError -> emptyList()
    }
    val id = episodeId.toIntOrNull()
    val ep = all.firstOrNull { it.id == id } ?: demoEpisodes(1).first().copy(id = id ?: 0)
    return Result.success(ep)
  }

  override suspend fun syncEpisodes(): Result<Unit> = Result.success(Unit)

  override suspend fun markLastRefreshNow() {
    _lastRefreshFlow.value = System.currentTimeMillis()
  }

  private class SuccessSource(
    private val items: List<Episode>, private val pageSize: Int
  ) : PagingSource<Int, Episode>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Episode> {
      val page = params.key ?: 0
      val from = page * pageSize
      if (from >= items.size) {
        return LoadResult.Page(emptyList(), prevKey = if (page == 0) null else page - 1, nextKey = null)
      }
      val to = (from + pageSize).coerceAtMost(items.size)
      val data = items.subList(from, to)
      return LoadResult.Page(data, prevKey = if (page == 0) null else page - 1, nextKey = if (to < items.size) page + 1 else null)
    }
    override fun getRefreshKey(state: PagingState<Int, Episode>) = 0
  }

  private class FirstLoadErrorSource : PagingSource<Int, Episode>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Episode> {
      return LoadResult.Error(Exception("Boom!"))
    }

    override fun getRefreshKey(state: PagingState<Int, Episode>): Int? = null
  }

  private class AppendErrorSource(
    private val failAfter: Int, private val pageSize: Int
  ) : PagingSource<Int, Episode>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Episode> {
      val page = params.key ?: 0
      val from = page * pageSize
      if (from >= failAfter) return LoadResult.Error(IllegalStateException("Nope!"))
      val limit = failAfter.coerceAtLeast(0)
      val to = (from + pageSize).coerceAtMost(limit)
      val data = demoEpisodes(limit).subList(from, to)
      val next = if (to < limit) page + 1 else page + 1 // next will error
      return LoadResult.Page(data, prevKey = if (page == 0) null else page - 1, nextKey = next)
    }
    override fun getRefreshKey(state: PagingState<Int, Episode>) = 0
  }

  private class EndlessSource(
    private val total: Int,
    private val pageSize: Int
  ) : PagingSource<Int, Episode>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Episode> {
      val page = params.key ?: 0
      val from = page * pageSize
      val to = (from + pageSize).coerceAtMost(total)
      val data = if (from < to) demoEpisodes(total).subList(from, to) else emptyList()
      val next = if (to < total) page + 1 else null
      return LoadResult.Page(
        data = data,
        prevKey = if (page == 0) null else page - 1,
        nextKey = next
      )
    }
    override fun getRefreshKey(state: PagingState<Int, Episode>) = 0
  }
}

fun demoEpisodes(count: Int): List<Episode> =
  (1..count).map {
    Episode(
      id = it,
      name = "Episode $it",
      airDate = "2017-01-$it",
      episodeCode = "S01E${String.format("%02d", it)}",
      characters = persistentListOf() ,
    )
  }

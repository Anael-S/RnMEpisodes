package com.anael.rickandmorty.data.paging

import com.anael.rickandmorty.data.remote.RnMApiService
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.anael.rickandmorty.data.model.EpisodeDto

class EpisodesPagingSource(
    private val service: RnMApiService,
) : PagingSource<Int, EpisodeDto>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, EpisodeDto> {
        val page = params.key ?: 1
        return try {
            val response = service.getEpisodesPage(page = page)
            val items = response.results

            //Allows to keep track when the loading is completed for the UI
            val nextKey = if (response.info.next == null || items.isEmpty()) null else page + 1

            val prevKey = if (page == 1) null else page - 1

            LoadResult.Page(
                data = items,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: retrofit2.HttpException) {
            // Rick & Morty API returns 404 when page is out of range -> treat as end of list
            if (e.code() == 404) {
                val prevKey = if (page == 1) null else page - 1
                LoadResult.Page(
                    data = emptyList(),
                    prevKey = prevKey,
                    nextKey = null
                )
            } else {
                LoadResult.Error(e)
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, EpisodeDto>): Int? {
        val anchor = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchor) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }
}

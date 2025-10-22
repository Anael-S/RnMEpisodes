package com.anael.rickandmorty.presentation.compose.episodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.anael.rickandmorty.R
import com.anael.rickandmorty.presentation.compose.utils.rememberFormatDateTime
import com.anael.rickandmorty.data.model.EpisodeDto
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.presentation.viewmodel.EpisodesViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
fun EpisodesScreen(
    viewModel: EpisodesViewModel = hiltViewModel(),
    onEpisodeClick: (Episode) -> Unit,
) {
    val lastRefresh by viewModel.lastRefreshTime.collectAsState(initial = null)
    val items = viewModel.episodes.collectAsLazyPagingItems()
    EpisodesScreen(
        pagedEpisodesItem = items,
        onEpisodeClick = onEpisodeClick,
        lastRefresh = lastRefresh
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodesScreen(
    pagedEpisodesItem: LazyPagingItems<Episode>,
    onEpisodeClick: (Episode) -> Unit = {},
    lastRefresh: Long? = null
) {
    val formatDateTime = rememberFormatDateTime()

    Scaffold(
        topBar = {
            GalleryTopBar(
                lastRefresh = lastRefresh,
                formatDateTime = formatDateTime
            )
        },
    ) { padding ->
        val pullToRefreshState = rememberPullToRefreshState()
        if (pullToRefreshState.isRefreshing) pagedEpisodesItem.refresh()



        LaunchedEffect(pagedEpisodesItem.loadState) {
            when (pagedEpisodesItem.loadState.refresh) {
                is LoadState.Loading -> Unit
                is LoadState.Error, is LoadState.NotLoading -> pullToRefreshState.endRefresh()
            }
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(all = dimensionResource(id = R.dimen.card_side_margin))
            ) {
                // Content items
                items(
                    count = pagedEpisodesItem.itemCount,
                    key = pagedEpisodesItem.itemKey { it.id }
                ) { index ->
                    val episode = pagedEpisodesItem[index] ?: return@items
                    EpisodeListItem(episode = episode) { onEpisodeClick(episode) }
                }

                // === FOOTER(s) ===
                // 1) Append loading
                if (pagedEpisodesItem.loadState.append is LoadState.Loading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FooterBox {
                            CircularProgressIndicator()
                        }
                    }
                }

                // 2) Append error with retry
                val appendError = pagedEpisodesItem.loadState.append as? LoadState.Error
                if (appendError != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FooterBox {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(id = R.string.error_loading_more),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                                Button(onClick = { pagedEpisodesItem.retry() }) {
                                    Text(stringResource(id = R.string.retry))
                                }
                            }
                        }
                    }
                }

                // 3) End of pagination reached
                val reachedEnd =
                    pagedEpisodesItem.loadState.append.endOfPaginationReached &&
                            pagedEpisodesItem.itemCount > 0 &&
                            pagedEpisodesItem.loadState.refresh is LoadState.NotLoading

                if (reachedEnd) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FooterBox(
                            modifier = Modifier.padding(
                                top = dimensionResource(id = R.dimen.margin_extra_small),
                                bottom = dimensionResource(id = R.dimen.margin_extra_small)
                            )
                        ) {
                            Text(
                                text = stringResource(id = R.string.nothing_more_to_load),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState
            )
        }
    }
}

@Composable
private fun FooterBox(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        content = content
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GalleryTopBar(
    modifier: Modifier = Modifier,
    lastRefresh: Long? = null,
    formatDateTime: (Long) -> String
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(id = R.string.episodes_title))
                Text(
                    text = lastRefresh?.let {
                        stringResource(id = R.string.last_updated) + formatDateTime(it)
                    } ?: stringResource(id = R.string.never_updated),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = modifier.statusBarsPadding(),
    )
}
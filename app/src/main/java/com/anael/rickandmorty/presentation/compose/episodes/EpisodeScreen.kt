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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.anael.rickandmorty.R
import com.anael.rickandmorty.domain.model.Episode
import com.anael.rickandmorty.presentation.compose.utils.rememberFormatDateTime
import com.anael.rickandmorty.presentation.viewmodel.EpisodesViewModel

object TestTags {
    const val EPISODES_LIST = "episodes_list"
    const val INITIAL_LOADER = "initial_loader"
    const val APPEND_LOADER = "append_loader"
    const val ERROR_EMPTY = "error_empty"
}

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

// 1) Trigger refresh only when the flag changes to true
        val isRefreshing = pullToRefreshState.isRefreshing
        LaunchedEffect(isRefreshing) {
            if (isRefreshing) {
                pagedEpisodesItem.refresh()
            }
        }

// 2) End the refresh when Paging finishes (success or error)
        LaunchedEffect(pagedEpisodesItem.loadState.refresh) {
            when (pagedEpisodesItem.loadState.refresh) {
                is LoadState.Loading -> Unit
                is LoadState.NotLoading, is LoadState.Error -> pullToRefreshState.endRefresh()
            }
        }


        val isInitialLoading =
            pagedEpisodesItem.loadState.refresh is LoadState.Loading &&
                    pagedEpisodesItem.itemCount == 0

        val refreshError = pagedEpisodesItem.loadState.refresh as? LoadState.Error

        Box(
            modifier = Modifier
                .padding(padding)
                .nestedScroll(pullToRefreshState.nestedScrollConnection)
        ) {
            when {
                // 1) First load: full-screen loader
                isInitialLoading -> {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = dimensionResource(id = R.dimen.margin_normal))
                    ) {
                        CircularProgressIndicator(
                            Modifier
                                .align(Alignment.TopCenter)
                                .testTag(TestTags.INITIAL_LOADER)
                        )
                    }
                }

                // 2) First load failed: full-screen error + retry
                refreshError != null && pagedEpisodesItem.itemCount == 0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(id = R.dimen.card_side_margin)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.error_loading_more),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.testTag(TestTags.ERROR_EMPTY)
                        )
                        Spacer(Modifier.height(dimensionResource(id = R.dimen.margin_small)))
                        Button(onClick = { pagedEpisodesItem.refresh() }) {
                            Text(stringResource(id = R.string.retry))
                        }
                    }
                }

                // 3) Normal content + footers
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        contentPadding = PaddingValues(all = dimensionResource(id = R.dimen.card_side_margin)),
                        modifier = Modifier.testTag(TestTags.EPISODES_LIST)
                    ) {
                        items(
                            count = pagedEpisodesItem.itemCount,
                            key = pagedEpisodesItem.itemKey { it.id }
                        ) { index ->
                            val episode = pagedEpisodesItem[index] ?: return@items
                            EpisodeListItem(episode = episode) { onEpisodeClick(episode) }
                        }

                        // Append loading footer
                        if (pagedEpisodesItem.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                FooterBox { CircularProgressIndicator(Modifier.testTag(TestTags.APPEND_LOADER)) }
                            }
                        }

                        // Append error footer
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

                        // End of pagination footer
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
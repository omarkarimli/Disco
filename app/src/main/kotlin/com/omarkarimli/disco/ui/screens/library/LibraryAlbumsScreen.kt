package com.omarkarimli.disco.ui.screens.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.omarkarimli.disco.LocalPlayerAwareWindowInsets
import com.omarkarimli.disco.LocalPlayerConnection
import com.omarkarimli.disco.R
import com.omarkarimli.disco.constants.AlbumFilter
import com.omarkarimli.disco.constants.AlbumFilterKey
import com.omarkarimli.disco.constants.AlbumSortDescendingKey
import com.omarkarimli.disco.constants.AlbumSortType
import com.omarkarimli.disco.constants.AlbumSortTypeKey
import com.omarkarimli.disco.constants.AlbumViewTypeKey
import com.omarkarimli.disco.constants.CONTENT_TYPE_ALBUM
import com.omarkarimli.disco.constants.CONTENT_TYPE_HEADER
import com.omarkarimli.disco.constants.GridItemSize
import com.omarkarimli.disco.constants.GridItemsSizeKey
import com.omarkarimli.disco.constants.GridThumbnailHeight
import com.omarkarimli.disco.constants.HideExplicitKey
import com.omarkarimli.disco.constants.LibraryViewType
import com.omarkarimli.disco.constants.YtmSyncKey
import com.omarkarimli.disco.ui.component.ChipsRow
import com.omarkarimli.disco.ui.component.EmptyPlaceholder
import com.omarkarimli.disco.ui.component.LibraryAlbumGridItem
import com.omarkarimli.disco.ui.component.LibraryAlbumListItem
import com.omarkarimli.disco.ui.component.LocalMenuState
import com.omarkarimli.disco.ui.component.SortHeader
import com.omarkarimli.disco.utils.rememberEnumPreference
import com.omarkarimli.disco.utils.rememberPreference
import com.omarkarimli.disco.viewmodels.LibraryAlbumsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryAlbumsScreen(
    navController: NavController,
    onDeselect: () -> Unit,
    viewModel: LibraryAlbumsViewModel = hiltViewModel(),
) {
    val layoutDirection = LocalLayoutDirection.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    var viewType by rememberEnumPreference(AlbumViewTypeKey, LibraryViewType.GRID)
    var filter by rememberEnumPreference(AlbumFilterKey, AlbumFilter.LIKED)
    val (sortType, onSortTypeChange) = rememberEnumPreference(
        AlbumSortTypeKey,
        AlbumSortType.CREATE_DATE
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(AlbumSortDescendingKey, true)
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    val (ytmSync) = rememberPreference(YtmSyncKey, true)
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

    val filterContent = @Composable {
        Row {
            Spacer(Modifier.width(12.dp))
            FilterChip(
                label = { Text(stringResource(R.string.albums)) },
                selected = true,
                colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = onDeselect,
                shape = MaterialTheme.shapes.large,
                leadingIcon = {
                    Icon(painter = painterResource(R.drawable.close), contentDescription = "")
                },
            )
            ChipsRow(
                chips =
                listOf(
                    AlbumFilter.LIKED to stringResource(R.string.filter_liked),
                    AlbumFilter.LIBRARY to stringResource(R.string.filter_library),
                    AlbumFilter.UPLOADED to stringResource(R.string.filter_uploaded)
                ),
                currentValue = filter,
                onValueUpdate = {
                    filter = it
                },
                modifier = Modifier.weight(1f),
            )
        }
    }

    LaunchedEffect(Unit) {
        if (ytmSync) {
            withContext(Dispatchers.IO) {
                viewModel.sync()
            }
        }
    }

    val albums by viewModel.allAlbums.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            when (viewType) {
                LibraryViewType.LIST -> lazyListState.animateScrollToItem(0)
                LibraryViewType.GRID -> lazyGridState.animateScrollToItem(0)
            }
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    val headerContent = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp),
        ) {
            SortHeader(
                sortType = sortType,
                sortDescending = sortDescending,
                onSortTypeChange = onSortTypeChange,
                onSortDescendingChange = onSortDescendingChange,
                sortTypeText = { sortType ->
                    when (sortType) {
                        AlbumSortType.CREATE_DATE -> R.string.sort_by_create_date
                        AlbumSortType.NAME -> R.string.sort_by_name
                        AlbumSortType.ARTIST -> R.string.sort_by_artist
                        AlbumSortType.YEAR -> R.string.sort_by_year
                        AlbumSortType.SONG_COUNT -> R.string.sort_by_song_count
                        AlbumSortType.LENGTH -> R.string.sort_by_length
                        AlbumSortType.PLAY_TIME -> R.string.sort_by_play_time
                    }
                },
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = pluralStringResource(R.plurals.n_album, albums.size, albums.size),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            IconButton(
                onClick = {
                    viewType = viewType.toggle()
                },
                modifier = Modifier.padding(start = 6.dp, end = 6.dp),
            ) {
                Icon(
                    painter =
                    painterResource(
                        when (viewType) {
                            LibraryViewType.LIST -> R.drawable.list
                            LibraryViewType.GRID -> R.drawable.grid_view
                        },
                    ),
                    contentDescription = null,
                )
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (viewType) {
            LibraryViewType.LIST -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                            .calculateStartPadding(layoutDirection),
                        end = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                            .calculateEndPadding(layoutDirection),
                        top = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                            .calculateTopPadding(),
                        bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues()
                            .calculateBottomPadding() + 24.dp
                    )
                ) {
                    item(
                        key = "filter",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    albums.let { albums ->
                        if (albums.isEmpty()) {
                            item(key = "empty_placeholder") {
                                EmptyPlaceholder(
                                    icon = R.drawable.album,
                                    text = stringResource(R.string.library_album_empty),
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        val filteredAlbumsForList = if (hideExplicit) {
                            albums.filter { !it.album.explicit }
                        } else {
                            albums
                        }
                        items(
                            items = filteredAlbumsForList.distinctBy { it.id },
                            key = { it.id },
                            contentType = { CONTENT_TYPE_ALBUM },
                        ) { album ->
                            LibraryAlbumListItem(
                                navController = navController,
                                menuState = menuState,
                                album = album,
                                isActive = album.id == mediaMetadata?.album?.id,
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .animateItem()
                            )
                        }
                    }
                }
            }
            LibraryViewType.GRID -> {
                LazyVerticalGrid(
                    state = lazyGridState,
                    columns =
                        GridCells.Adaptive(
                            minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp,
                        ),
                    contentPadding = PaddingValues(
                        start = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateStartPadding(layoutDirection),
                        end = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateEndPadding(layoutDirection),
                        top = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding(),
                        bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 24.dp
                    )
                ) {
                    item(
                        key = "filter",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        filterContent()
                    }

                    item(
                        key = "header",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = CONTENT_TYPE_HEADER,
                    ) {
                        headerContent()
                    }

                    albums.let { albums ->
                        if (albums.isEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                EmptyPlaceholder(
                                    icon = R.drawable.album,
                                    text = stringResource(R.string.library_album_empty),
                                    modifier = Modifier.animateItem()
                                )
                            }
                        }

                        val filteredAlbumsForGrid = if (hideExplicit) {
                            albums.filter { !it.album.explicit }
                        } else {
                            albums
                        }
                        items(
                            items = filteredAlbumsForGrid.distinctBy { it.id },
                            key = { it.id },
                            contentType = { CONTENT_TYPE_ALBUM },
                        ) { album ->
                            LibraryAlbumGridItem(
                                navController = navController,
                                menuState = menuState,
                                album = album,
                                isActive = album.id == mediaMetadata?.album?.id,
                                isPlaying = isPlaying,
                                modifier = Modifier
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }
    }
}

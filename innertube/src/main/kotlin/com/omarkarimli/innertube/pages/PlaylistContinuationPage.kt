package com.omarkarimli.innertube.pages

import com.omarkarimli.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)

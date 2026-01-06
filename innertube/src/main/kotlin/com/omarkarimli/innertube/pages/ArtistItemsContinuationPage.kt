package com.omarkarimli.innertube.pages

import com.omarkarimli.innertube.models.YTItem

data class ArtistItemsContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)

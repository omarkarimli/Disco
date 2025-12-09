package com.omarkarimli.innertube.pages

import com.omarkarimli.innertube.models.YTItem

data class LibraryContinuationPage(
    val items: List<YTItem>,
    val continuation: String?,
)

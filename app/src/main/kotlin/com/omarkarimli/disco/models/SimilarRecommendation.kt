package com.omarkarimli.disco.models

import com.omarkarimli.innertube.models.YTItem
import com.omarkarimli.disco.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)

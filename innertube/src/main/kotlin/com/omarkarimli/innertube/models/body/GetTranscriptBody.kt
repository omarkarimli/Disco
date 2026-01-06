package com.omarkarimli.innertube.models.body

import com.omarkarimli.innertube.models.Context
import kotlinx.serialization.Serializable

@Serializable
data class GetTranscriptBody(
    val context: Context,
    val params: String,
)

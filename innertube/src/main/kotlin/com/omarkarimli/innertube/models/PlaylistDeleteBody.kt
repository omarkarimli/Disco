package com.omarkarimli.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistDeleteBody(
    val context: Context,
    val playlistId: String
)
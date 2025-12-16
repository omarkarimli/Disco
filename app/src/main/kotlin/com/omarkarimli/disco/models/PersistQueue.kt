package com.omarkarimli.disco.models

import java.io.Serializable

data class PersistQueue(
    val title: String?,
    val items: List<MediaMetadata>,
    val mediaItemIndex: Int,
    val position: Long,
    val queueType: QueueType = QueueType.List,
    val queueData: QueueData? = null,
) : Serializable

sealed class QueueType : Serializable {
    object List : QueueType() {
        private fun readResolve(): Any = List
    }

    object Youtube : QueueType() {
        private fun readResolve(): Any = Youtube
    }

    object YoutubeAlbumRadio : QueueType() {
        private fun readResolve(): Any = YoutubeAlbumRadio
    }

    object LocalAlbumRadio : QueueType() {
        private fun readResolve(): Any = LocalAlbumRadio
    }
}

sealed class QueueData : Serializable {
    data class YouTubeData(
        val endpoint: String,
        val continuation: String? = null
    ) : QueueData()
    
    data class YouTubeAlbumRadioData(
        val playlistId: String,
        val albumSongCount: Int = 0,
        val continuation: String? = null,
        val firstTimeLoaded: Boolean = false
    ) : QueueData()
    
    data class LocalAlbumRadioData(
        val albumId: String,
        val startIndex: Int = 0,
        val playlistId: String? = null,
        val continuation: String? = null,
        val firstTimeLoaded: Boolean = false
    ) : QueueData()
}

package com.omarkarimli.disco.lyrics

import android.content.Context
import com.omarkarimli.kugou.KuGou
import com.omarkarimli.disco.constants.EnableKugouKey
import com.omarkarimli.disco.utils.dataStore
import com.omarkarimli.disco.utils.get

object KuGouLyricsProvider : LyricsProvider {
    override val name = "Kugou"
    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableKugouKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int
    ): Result<String> =
        KuGou.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit
    ) {
        KuGou.getAllPossibleLyricsOptions(title, artist, duration, callback)
    }
}

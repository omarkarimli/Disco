package com.omarkarimli.disco.extensions

import com.omarkarimli.disco.db.entities.Song
import com.omarkarimli.disco.db.entities.Album

fun <T> List<T>.reversed(reversed: Boolean) = if (reversed) asReversed() else this

fun <T> MutableList<T>.move(
    fromIndex: Int,
    toIndex: Int,
): MutableList<T> {
    add(toIndex, removeAt(fromIndex))
    return this
}

// Extension function to filter explicit content for local Song entities
fun List<Song>.filterExplicit(enabled: Boolean = true) =
    if (enabled) {
        filter { !it.song.explicit }
    } else {
        this
    }

// Extension function to filter explicit content for local Album entities
fun List<Album>.filterExplicitAlbums(enabled: Boolean = true) =
    if (enabled) {
        filter { !it.album.explicit }
    } else {
        this
    }

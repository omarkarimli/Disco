package com.omarkarimli.disco.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.omarkarimli.innertube.YouTube
import com.omarkarimli.innertube.models.AlbumItem
import com.omarkarimli.innertube.models.ArtistItem
import com.omarkarimli.innertube.models.PlaylistItem
import com.omarkarimli.innertube.utils.completed
import com.omarkarimli.disco.utils.reportException
import com.omarkarimli.disco.ui.utils.resize
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AccountContentType {
    PLAYLISTS, ALBUMS, ARTISTS
}

@HiltViewModel
class AccountViewModel @Inject constructor() : ViewModel() {
    val playlists = MutableStateFlow<List<PlaylistItem>?>(null)
    val albums = MutableStateFlow<List<AlbumItem>?>(null)
    val artists = MutableStateFlow<List<ArtistItem>?>(null)
    
    // Selected content type for chips
    val selectedContentType = MutableStateFlow(AccountContentType.PLAYLISTS)

    init {
        viewModelScope.launch {
            YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
                playlists.value = it.items
                    .filterIsInstance<PlaylistItem>()
                    .filterNot { item -> item.id == "SE" }
            }.onFailure {
                reportException(it)
            }
            YouTube.library("FEmusic_liked_albums").completed().onSuccess {
                albums.value = it.items.filterIsInstance<AlbumItem>()
            }.onFailure {
                reportException(it)
            }
            YouTube.library("FEmusic_library_corpus_artists").completed().onSuccess {
                artists.value = it.items.filterIsInstance<ArtistItem>().map { artist ->
                    artist.copy(
                        thumbnail = artist.thumbnail?.resize(544, 544)
                    )
                }
            }.onFailure {
                reportException(it)
            }
        }
    }
    
    fun setSelectedContentType(contentType: AccountContentType) {
        selectedContentType.value = contentType
    }
}

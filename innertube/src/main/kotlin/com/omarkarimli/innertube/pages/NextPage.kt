package com.omarkarimli.innertube.pages

import com.omarkarimli.innertube.models.Album
import com.omarkarimli.innertube.models.Artist
import com.omarkarimli.innertube.models.BrowseEndpoint
import com.omarkarimli.innertube.models.PlaylistPanelVideoRenderer
import com.omarkarimli.innertube.models.SongItem
import com.omarkarimli.innertube.models.WatchEndpoint
import com.omarkarimli.innertube.models.oddElements
import com.omarkarimli.innertube.models.splitBySeparator
import com.omarkarimli.innertube.utils.parseTime

data class NextResult(
    val title: String? = null,
    val items: List<SongItem>,
    val currentIndex: Int? = null,
    val lyricsEndpoint: BrowseEndpoint? = null,
    val relatedEndpoint: BrowseEndpoint? = null,
    val continuation: String?,
    val endpoint: WatchEndpoint, // current or continuation next endpoint
)

object NextPage {
    fun fromPlaylistPanelVideoRenderer(renderer: PlaylistPanelVideoRenderer): SongItem? {
        val longByLineRuns = renderer.longBylineText?.runs?.splitBySeparator() ?: return null
        return SongItem(
            id = renderer.videoId ?: return null,
            title =
                renderer.title
                    ?.runs
                    ?.firstOrNull()
                    ?.text ?: return null,
            artists =
                longByLineRuns.firstOrNull()?.oddElements()?.map {
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId,
                    )
                } ?: return null,
            album =
                longByLineRuns
                    .getOrNull(1)
                    ?.firstOrNull()
                    ?.takeIf {
                        it.navigationEndpoint?.browseEndpoint != null
                    }?.let {
                        Album(
                            name = it.text,
                            id = it.navigationEndpoint?.browseEndpoint?.browseId!!,
                        )
                    },
            duration =
                renderer.lengthText
                    ?.runs
                    ?.firstOrNull()
                    ?.text
                    ?.parseTime() ?: return null,
            thumbnail =
                renderer.thumbnail.thumbnails
                    .lastOrNull()
                    ?.url ?: return null,
            explicit =
                renderer.badges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
            libraryAddToken = PageHelper.extractFeedbackToken(renderer.menu?.menuRenderer?.items?.find {
                it.toggleMenuServiceItemRenderer?.defaultIcon?.iconType?.startsWith("LIBRARY_") == true
            }?.toggleMenuServiceItemRenderer, "LIBRARY_ADD"),
            libraryRemoveToken = PageHelper.extractFeedbackToken(renderer.menu?.menuRenderer?.items?.find {
                it.toggleMenuServiceItemRenderer?.defaultIcon?.iconType?.startsWith("LIBRARY_") == true
            }?.toggleMenuServiceItemRenderer, "LIBRARY_SAVED")
        )
    }
}

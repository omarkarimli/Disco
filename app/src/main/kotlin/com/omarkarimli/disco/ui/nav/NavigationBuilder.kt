package com.omarkarimli.disco.ui.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.omarkarimli.disco.ui.screens.AccountScreen
import com.omarkarimli.disco.ui.screens.AlbumScreen
import com.omarkarimli.disco.ui.screens.BrowseScreen
import com.omarkarimli.disco.ui.screens.CenterScreen
import com.omarkarimli.disco.ui.screens.ChartsScreen
import com.omarkarimli.disco.ui.screens.HistoryScreen
import com.omarkarimli.disco.ui.screens.HomeScreen
import com.omarkarimli.disco.ui.screens.LoginScreen
import com.omarkarimli.disco.ui.screens.MoodAndGenresScreen
import com.omarkarimli.disco.ui.screens.NewReleaseScreen
import com.omarkarimli.disco.ui.screens.StatsScreen
import com.omarkarimli.disco.ui.screens.YouTubeBrowseScreen
import com.omarkarimli.disco.ui.screens.artist.ArtistAlbumsScreen
import com.omarkarimli.disco.ui.screens.artist.ArtistItemsScreen
import com.omarkarimli.disco.ui.screens.artist.ArtistScreen
import com.omarkarimli.disco.ui.screens.artist.ArtistSongsScreen
import com.omarkarimli.disco.ui.screens.library.LibraryScreen
import com.omarkarimli.disco.ui.screens.playlist.AutoPlaylistScreen
import com.omarkarimli.disco.ui.screens.playlist.LocalPlaylistScreen
import com.omarkarimli.disco.ui.screens.playlist.OnlinePlaylistScreen
import com.omarkarimli.disco.ui.screens.playlist.TopPlaylistScreen
import com.omarkarimli.disco.ui.screens.playlist.CachePlaylistScreen
import com.omarkarimli.disco.ui.screens.search.OnlineSearchResult
import com.omarkarimli.disco.ui.screens.settings.AboutScreen
import com.omarkarimli.disco.ui.screens.settings.AppearanceSettings
import com.omarkarimli.disco.ui.screens.settings.BackupAndRestore
import com.omarkarimli.disco.ui.screens.settings.ContentSettings
import com.omarkarimli.disco.ui.screens.settings.DiscordLoginScreen
import com.omarkarimli.disco.ui.screens.settings.integrations.DiscordSettings
import com.omarkarimli.disco.ui.screens.settings.integrations.IntegrationScreen
import com.omarkarimli.disco.ui.screens.settings.integrations.LastFMSettings
import com.omarkarimli.disco.ui.screens.settings.PlayerSettings
import com.omarkarimli.disco.ui.screens.settings.PrivacySettings
import com.omarkarimli.disco.ui.screens.settings.RomanizationSettings
import com.omarkarimli.disco.ui.screens.settings.SettingsScreen
import com.omarkarimli.disco.ui.screens.settings.StorageSettings
import com.omarkarimli.disco.ui.screens.settings.UpdaterScreen


@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.navigationBuilder(
    navController: NavHostController,
    scrollBehavior: TopAppBarScrollBehavior,
    latestVersionName: String,
) {
    composable(Screens.Home.route) {
        HomeScreen(navController)
    }
    composable(Screens.Library.route) {
        LibraryScreen(navController)
    }
    composable(Screens.Center.route) {
        CenterScreen(navController, latestVersionName)
    }
    composable("history") {
        HistoryScreen(navController)
    }
    composable("stats") {
        StatsScreen(navController)
    }
    composable("mood_and_genres") {
        MoodAndGenresScreen(navController, scrollBehavior)
    }
    composable("account") {
        AccountScreen(navController)
    }
    composable("new_release") {
        NewReleaseScreen(navController, scrollBehavior)
    }
    composable("charts_screen") {
        ChartsScreen(navController)
    }
    composable(
        route = "browse/{browseId}",
        arguments = listOf(
            navArgument("browseId") {
                type = NavType.StringType
            }
        )
    ) {
        BrowseScreen(
            navController
        )
    }
    composable(
        route = "search/{query}",
        arguments =
        listOf(
            navArgument("query") {
                type = NavType.StringType
            },
        ),
        enterTransition = {
            fadeIn(tween(250))
        },
        exitTransition = {
            if (targetState.destination.route?.startsWith("search/") == true) {
                fadeOut(tween(200))
            } else {
                fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
            }
        },
        popEnterTransition = {
            if (initialState.destination.route?.startsWith("search/") == true) {
                fadeIn(tween(250))
            } else {
                fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
            }
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
    ) {
        OnlineSearchResult(navController)
    }
    composable(
        route = "album/{albumId}",
        arguments =
        listOf(
            navArgument("albumId") {
                type = NavType.StringType
            },
        ),
    ) {
        AlbumScreen(navController)
    }
    composable(
        route = "artist/{artistId}",
        arguments =
        listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistScreen(navController)
    }
    composable(
        route = "artist/{artistId}/songs",
        arguments =
        listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
        ),
    ) {
        ArtistSongsScreen(navController)
    }
    composable(
        route = "artist/{artistId}/albums",
        arguments = listOf(
            navArgument("artistId") {
                type = NavType.StringType
            }
        )
    ) {
        ArtistAlbumsScreen(navController, scrollBehavior)
    }
    composable(
        route = "artist/{artistId}/items?browseId={browseId}?params={params}",
        arguments =
        listOf(
            navArgument("artistId") {
                type = NavType.StringType
            },
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        ArtistItemsScreen(navController)
    }
    composable(
        route = "online_playlist/{playlistId}",
        arguments =
        listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        OnlinePlaylistScreen(navController)
    }
    composable(
        route = "local_playlist/{playlistId}",
        arguments =
        listOf(
            navArgument("playlistId") {
                type = NavType.StringType
            },
        ),
    ) {
        LocalPlaylistScreen(navController)
    }
    composable(
        route = "auto_playlist/{playlist}",
        arguments =
        listOf(
            navArgument("playlist") {
                type = NavType.StringType
            },
        ),
    ) {
        AutoPlaylistScreen(navController)
    }
    composable(
        route = "cache_playlist/{playlist}",
        arguments =
            listOf(
                navArgument("playlist") {
                    type = NavType.StringType
            },
        ),
    ) {
        CachePlaylistScreen(navController)
    }
    composable(
        route = "top_playlist/{top}",
        arguments =
        listOf(
            navArgument("top") {
                type = NavType.StringType
            },
        ),
    ) {
        TopPlaylistScreen(navController)
    }
    composable(
        route = "youtube_browse/{browseId}?params={params}",
        arguments =
        listOf(
            navArgument("browseId") {
                type = NavType.StringType
                nullable = true
            },
            navArgument("params") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        YouTubeBrowseScreen(navController)
    }
    composable("settings") {
        SettingsScreen(navController, scrollBehavior, latestVersionName)
    }
    composable("settings/appearance") {
        AppearanceSettings(navController)
    }
    composable("settings/content") {
        ContentSettings(navController)
    }
    composable("settings/content/romanization") {
        RomanizationSettings(navController, scrollBehavior)
    }
    composable("settings/player") {
        PlayerSettings(navController, scrollBehavior)
    }
    composable("settings/storage") {
        StorageSettings(navController, scrollBehavior)
    }
    composable("settings/privacy") {
        PrivacySettings(navController, scrollBehavior)
    }
    composable("settings/backup_restore") {
        BackupAndRestore(navController)
    }
    composable("settings/integrations") {
        IntegrationScreen(navController)
    }
    composable("settings/integrations/discord") {
        DiscordSettings(navController)
    }
    composable("settings/integrations/lastfm") {
        LastFMSettings(navController)
    }
    composable("settings/discord/login") {
        DiscordLoginScreen(navController)
    }
    composable("settings/updater") {
        UpdaterScreen(navController)
    }
    composable("settings/about") {
        AboutScreen(navController)
    }
    composable("login") {
        LoginScreen(navController)
    }
}

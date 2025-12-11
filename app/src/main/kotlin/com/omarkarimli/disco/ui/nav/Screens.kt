package com.omarkarimli.disco.ui.nav

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.omarkarimli.disco.R
import com.omarkarimli.disco.ui.nav.Screens.Home
import com.omarkarimli.disco.ui.nav.Screens.Library
import com.omarkarimli.disco.ui.nav.Screens.Center

val BottomBarItems = listOf(Home, Library, Center)

val RouteToTitleMap = BottomBarItems.associate { screen ->
    screen.route to screen.titleId
}

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconIdInactive: Int,
    @DrawableRes val iconIdActive: Int,
    val route: String,
) {
    object Home : Screens(
        titleId = R.string.explore,
        iconIdInactive = R.drawable.search,
        iconIdActive = R.drawable.search_filled,
        route = "home"
    )

    object Library : Screens(
        titleId = R.string.filter_library,
        iconIdInactive = R.drawable.library_music_outlined,
        iconIdActive = R.drawable.library_music_filled,
        route = "library"
    )

    object Center : Screens(
        titleId = R.string.center,
        iconIdInactive = R.drawable.explore_outlined,
        iconIdActive = R.drawable.explore_filled,
        route = "center"
    )
}

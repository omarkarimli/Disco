package com.omarkarimli.disco.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.omarkarimli.disco.R

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

    object Profile : Screens(
        titleId = R.string.account,
        iconIdInactive = R.drawable.account_outlined,
        iconIdActive = R.drawable.account_filled,
        route = "account"
    )

    companion object {
        val BottomBarItems = listOf(Home, Library, Profile)
    }
}

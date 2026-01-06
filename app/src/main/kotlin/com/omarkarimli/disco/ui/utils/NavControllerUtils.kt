package com.omarkarimli.disco.ui.utils

import androidx.navigation.NavController
import com.omarkarimli.disco.ui.nav.BottomBarItems

fun NavController.backToMain() {
    val bottomRoutes = BottomBarItems.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in bottomRoutes
    ) {
        popBackStack()
    }
}
package com.omarkarimli.disco.ui.utils

import androidx.navigation.NavController
import com.omarkarimli.disco.ui.screens.Screens

fun NavController.backToMain() {
    val bottomRoutes = Screens.BottomBarItems.map { it.route }

    while (previousBackStackEntry != null &&
        currentBackStackEntry?.destination?.route !in bottomRoutes
    ) {
        popBackStack()
    }
}
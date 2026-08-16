package com.islamichub.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Quran : Screen("quran")
    data object QuranReader : Screen("quran/{surahNumber}") {
        fun createRoute(surahNumber: Int) = "quran/$surahNumber"
    }
    data object Prayer : Screen("prayer")
    data object Qibla : Screen("qibla")
    data object Tasbih : Screen("tasbih")
    data object Names : Screen("names")
    data object Duas : Screen("duas")
    data object DuaDetail : Screen("dua/{duaId}") {
        fun createRoute(id: String) = "dua/$id"
    }
    data object Calendar : Screen("calendar")
}

data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(Screen.Home, com.islamichub.app.R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Screen.Quran, com.islamichub.app.R.string.nav_quran, Icons.Filled.AutoStories),
    BottomNavItem(Screen.Prayer, com.islamichub.app.R.string.nav_prayer, Icons.Filled.MenuBook),
    BottomNavItem(Screen.Qibla, com.islamichub.app.R.string.nav_more, Icons.Filled.MoreHoriz)
)

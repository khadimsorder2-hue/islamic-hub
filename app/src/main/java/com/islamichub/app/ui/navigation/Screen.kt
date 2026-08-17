package com.islamichub.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Quran : Screen("quran")
    data object QuranSearch : Screen("quran/search")
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
    data object Settings : Screen("settings")
    data object Profile : Screen("profile")
    data object More : Screen("more")

    // Hadith
    data object Hadith : Screen("hadith")
    data object HadithTopics : Screen("hadith_topics")
    data object HadithDetail : Screen("hadith/{collection}/{hadithNumber}") {
        fun createRoute(collection: String, hadithNumber: Int) = "hadith/$collection/$hadithNumber"
    }
    data object HadithSearch : Screen("hadith/search")

    // Quran extras
    data object Bookmarks : Screen("bookmarks")
    data object Tafsir : Screen("tafsir/{surah}/{ayah}") {
        fun createRoute(surah: Int, ayah: Int) = "tafsir/$surah/$ayah"
    }
    data object Khatam : Screen("khatam")

    // Trackers
    data object Qada : Screen("qada")
    data object Tracker : Screen("tracker")

    // New v1.3.0 screens
    data object Misconceptions : Screen("misconceptions")
    data object NamazShikkha : Screen("namaz_shikkha")
    data object NamazExtras : Screen("namaz_extras")
    data object AiScholar : Screen("ai_scholar")
    data object TajweedChecker : Screen("tajweed_checker")
    data object Scanner : Screen("scanner")
    data object Stories : Screen("stories")
    data object Kalima : Screen("kalima")
    data object Qa : Screen("qa")

    // New v3.1.0 screens
    data object Zakat : Screen("zakat")
    data object Quiz : Screen("quiz")
    data object Fasting : Screen("fasting")

    // New v3.2.0 screens — Thematic Quran Study
    data object TopicStudyList : Screen("topic_study")
    data object TopicStudyDetail : Screen("topic_study/{slug}") {
        fun createRoute(slug: String) = "topic_study/$slug"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
)

val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(Screen.Home, com.islamichub.app.R.string.nav_home, Icons.Filled.Home),
    BottomNavItem(Screen.Quran, com.islamichub.app.R.string.nav_quran, Icons.Filled.AutoStories),
    BottomNavItem(Screen.Hadith, com.islamichub.app.R.string.nav_hadith, Icons.Filled.Book),
    BottomNavItem(Screen.More, com.islamichub.app.R.string.nav_more, Icons.Filled.Dashboard)
)

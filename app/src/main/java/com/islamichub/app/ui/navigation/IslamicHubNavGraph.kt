package com.islamichub.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.screens.bookmarks.BookmarksScreen
import com.islamichub.app.ui.screens.calendar.CalendarScreen
import com.islamichub.app.ui.screens.dua.DuaDetailScreen
import com.islamichub.app.ui.screens.dua.DuaListScreen
import com.islamichub.app.ui.screens.hadith.HadithCollectionScreen
import com.islamichub.app.ui.screens.hadith.HadithDetailScreen
import com.islamichub.app.ui.screens.hadith.HadithListScreen
import com.islamichub.app.ui.screens.hadith.HadithSearchScreen
import com.islamichub.app.ui.screens.home.HomeScreen
import com.islamichub.app.ui.screens.khatam.KhatamScreen
import com.islamichub.app.ui.screens.more.MoreScreen
import com.islamichub.app.ui.screens.names.NamesScreen
import com.islamichub.app.ui.screens.prayer.PrayerScreen
import com.islamichub.app.ui.screens.profile.ProfileScreen
import com.islamichub.app.ui.screens.qibla.QiblaScreen
import com.islamichub.app.ui.screens.quran.QariSelectorSheet
import com.islamichub.app.ui.screens.quran.QuranListScreen
import com.islamichub.app.ui.screens.quran.QuranReaderScreen
import com.islamichub.app.ui.screens.quran.QuranSearchScreen
import com.islamichub.app.ui.screens.settings.SettingsScreen
import com.islamichub.app.ui.screens.tasbih.TasbihScreen
import com.islamichub.app.ui.screens.tracker.TrackerScreen
import com.islamichub.app.ui.screens.qada.QadaScreen

@Composable
fun IslamicHubNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Screen.Home.route,
        Screen.Quran.route,
        Screen.Hadith.route,
        Screen.More.route
    )

    var showQariSelector by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.labelRes)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    container = container,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Quran.route) {
                QuranListScreen(
                    container = container,
                    onSurahClick = { num ->
                        navController.navigate(Screen.QuranReader.createRoute(num))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.QuranSearch.route)
                    }
                )
            }
            composable(Screen.QuranSearch.route) {
                QuranSearchScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onAyahClick = { num ->
                        navController.navigate(Screen.QuranReader.createRoute(num))
                    }
                )
            }
            composable(
                route = Screen.QuranReader.route,
                arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
            ) { backStackEntry ->
                val num = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                QuranReaderScreen(
                    container = container,
                    surahNumber = num,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Prayer.route) {
                PrayerScreen(container = container)
            }
            composable(Screen.Qibla.route) {
                QiblaScreen(container = container)
            }
            composable(Screen.Tasbih.route) {
                TasbihScreen(container = container)
            }
            composable(Screen.Names.route) {
                NamesScreen(container = container)
            }
            composable(Screen.Duas.route) {
                DuaListScreen(
                    container = container,
                    onDuaClick = { id -> navController.navigate(Screen.DuaDetail.createRoute(id)) }
                )
            }
            composable(
                route = Screen.DuaDetail.route,
                arguments = listOf(navArgument("duaId") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("duaId").orEmpty()
                DuaDetailScreen(container = container, duaId = id, onBack = { navController.popBackStack() })
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(container = container)
            }

            // ─── Hadith ────────────────────────────────────────────────
            composable(Screen.Hadith.route) {
                HadithListScreen(
                    container = container,
                    onCollectionClick = { id ->
                        navController.navigate("hadith_collection/$id")
                    },
                    onSearchClick = { navController.navigate(Screen.HadithSearch.route) }
                )
            }
            composable(
                route = "hadith_collection/{collection}",
                arguments = listOf(navArgument("collection") { type = NavType.StringType })
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collection").orEmpty()
                HadithCollectionScreen(
                    container = container,
                    collectionId = collectionId,
                    onBack = { navController.popBackStack() },
                    onHadithClick = { coll, num ->
                        navController.navigate(Screen.HadithDetail.createRoute(coll, num))
                    }
                )
            }
            composable(
                route = Screen.HadithDetail.route,
                arguments = listOf(
                    navArgument("collection") { type = NavType.StringType },
                    navArgument("hadithNumber") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val collectionId = backStackEntry.arguments?.getString("collection").orEmpty()
                val hadithNumber = backStackEntry.arguments?.getInt("hadithNumber") ?: 1
                HadithDetailScreen(
                    container = container,
                    collectionId = collectionId,
                    hadithNumber = hadithNumber,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.HadithSearch.route) {
                HadithSearchScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onHadithClick = { coll, num ->
                        navController.navigate(Screen.HadithDetail.createRoute(coll, num))
                    }
                )
            }

            // ─── More screens ─────────────────────────────────────────
            composable(Screen.More.route) {
                MoreScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(Screen.Qada.route) {
                QadaScreen(container = container, onBack = { navController.popBackStack() })
            }
            composable(Screen.Tracker.route) {
                TrackerScreen(container = container, onBack = { navController.popBackStack() })
            }
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onBookmarkClick = { num ->
                        navController.navigate(Screen.QuranReader.createRoute(num))
                    }
                )
            }
            composable(Screen.Khatam.route) {
                KhatamScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onSurahClick = { num ->
                        navController.navigate(Screen.QuranReader.createRoute(num))
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(container = container, onBack = { navController.popBackStack() })
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onShowQariSelector = { showQariSelector = true }
                )
            }

            // v1.3.0 screens
            composable(Screen.Misconceptions.route) {
                com.islamichub.app.ui.screens.misconceptions.MisconceptionsScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.NamazShikkha.route) {
                com.islamichub.app.ui.screens.namaz.NamazShikkhaScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.NamazExtras.route) {
                com.islamichub.app.ui.screens.namaz_extras.NamazExtrasScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AiScholar.route) {
                com.islamichub.app.ui.screens.ai_scholar.AiScholarScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.TajweedChecker.route) {
                com.islamichub.app.ui.screens.tajweed.TajweedCheckerScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Scanner.route) {
                com.islamichub.app.ui.screens.scanner.ScannerScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Stories.route) {
                com.islamichub.app.ui.screens.stories.StoriesScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Kalima.route) {
                com.islamichub.app.ui.screens.kalima.KalimaScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Qa.route) {
                com.islamichub.app.ui.screens.qa.QaScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showQariSelector) {
        QariSelectorSheet(
            container = container,
            onDismiss = { showQariSelector = false }
        )
    }
}

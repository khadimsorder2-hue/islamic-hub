package com.islamichub.app.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamichub.app.data.AppContainer
import com.islamichub.app.ui.screens.calendar.CalendarScreen
import com.islamichub.app.ui.screens.dua.DuaDetailScreen
import com.islamichub.app.ui.screens.dua.DuaListScreen
import com.islamichub.app.ui.screens.home.HomeScreen
import com.islamichub.app.ui.screens.names.NamesScreen
import com.islamichub.app.ui.screens.prayer.PrayerScreen
import com.islamichub.app.ui.screens.qibla.QiblaScreen
import com.islamichub.app.ui.screens.quran.QuranListScreen
import com.islamichub.app.ui.screens.quran.QuranReaderScreen
import com.islamichub.app.ui.screens.quran.QuranSearchScreen
import com.islamichub.app.ui.screens.tasbih.TasbihScreen

@Composable
fun IslamicHubNavGraph(container: AppContainer) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val showBottomBar = currentRoute in setOf(
        Screen.Home.route,
        Screen.Quran.route,
        Screen.Prayer.route,
        Screen.Qibla.route
    )

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
        }
    }
}

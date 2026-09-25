package com.islamichub.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.dp
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
            Column {
                // Floating audio player — ALWAYS visible (above nav bar on all screens).
                // v5.9.0 — tapping the title row opens that surah in the reader, and the
                // player pads itself for the gesture bar on screens without the nav bar.
                com.islamichub.app.ui.components.FloatingAudioPlayer(
                    container = container,
                    padForNavigationBar = !showBottomBar,
                    onOpenReader = { surah ->
                        navController.navigate(Screen.QuranReader.createRoute(surah))
                    }
                )
                if (showBottomBar) {
                    // Premium glassmorphism nav bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        shadowElevation = 8.dp
                    ) {
                        NavigationBar(
                            containerColor = androidx.compose.ui.graphics.Color.Transparent,
                            tonalElevation = 0.dp
                        ) {
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
                                    icon = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (selected)
                                                        Brush.linearGradient(
                                                            colors = listOf(
                                                                MaterialTheme.colorScheme.primary,
                                                                MaterialTheme.colorScheme.secondary
                                                            )
                                                        )
                                                    else Brush.linearGradient(
                                                        colors = listOf(
                                                            androidx.compose.ui.graphics.Color.Transparent,
                                                            androidx.compose.ui.graphics.Color.Transparent
                                                        )
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = null,
                                                tint = if (selected) androidx.compose.ui.graphics.Color.White
                                                       else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = stringResource(item.labelRes),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (selected) MaterialTheme.colorScheme.primary
                                                   else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                    initialOffsetX = { it / 6 }
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { -it / 8 }
                ) + fadeOut(animationSpec = tween(260))
            },
            popEnterTransition = {
                slideInHorizontally(
                    animationSpec = tween(340, easing = FastOutSlowInEasing),
                    initialOffsetX = { -it / 6 }
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                    targetOffsetX = { it / 8 }
                ) + fadeOut(animationSpec = tween(260))
            }
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
                        navController.navigate(Screen.QuranSearch.createRoute(""))
                    }
                )
            }
            composable(
                route = Screen.QuranSearch.route,
                arguments = listOf(navArgument("q") {
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                QuranSearchScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onAyahClick = { num ->
                        navController.navigate(Screen.QuranReader.createRoute(num))
                    },
                    initialQuery = backStackEntry.arguments?.getString("q") ?: ""
                )
            }
            composable(
                route = Screen.QuranReader.route,
                arguments = listOf(
                    navArgument("surahNumber") { type = NavType.IntType },
                    // v5.11.0 — optional ayah jump (bookmarks / resume reading)
                    navArgument("ayah") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val num = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                val ayah = backStackEntry.arguments?.getInt("ayah") ?: -1
                QuranReaderScreen(
                    container = container,
                    surahNumber = num,
                    initialAyah = if (ayah >= 1) ayah else null,
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
                    onBack = { navController.popBackStack() },
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
                CalendarScreen(container = container, onBack = { navController.popBackStack() })
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
                    onBack = { navController.popBackStack() },
                    onNavigateHadith = { num ->
                        // v5.11.0 — sequential reading without going back to the list
                        navController.navigate(Screen.HadithDetail.createRoute(collectionId, num))
                    }
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
            composable(Screen.HadithTopics.route) {
                com.islamichub.app.ui.screens.hadith.HadithTopicsScreen(
                    container = container,
                    onBack = { navController.popBackStack() }
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
                    onBookmarkClick = { num, ayah ->
                        // v5.11.0 — jump straight to the saved ayah (was surah-top only)
                        navController.navigate(Screen.QuranReader.createRoute(num, ayah))
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

            // v3.1.0 screens
            composable(Screen.Zakat.route) {
                com.islamichub.app.ui.screens.zakat.ZakatScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Quiz.route) {
                com.islamichub.app.ui.screens.quiz.QuizScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Fasting.route) {
                com.islamichub.app.ui.screens.fasting.FastingScreen(
                    container = container, onBack = { navController.popBackStack() }
                )
            }

            // v3.2.0 — Thematic Quran Study
            composable(Screen.TopicStudyList.route) {
                com.islamichub.app.ui.screens.topic_study.TopicStudyListScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onTopicClick = { slug -> navController.navigate(Screen.TopicStudyDetail.createRoute(slug)) }
                )
            }
            composable(
                route = Screen.TopicStudyDetail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                com.islamichub.app.ui.screens.topic_study.TopicStudyDetailScreen(
                    container = container,
                    topicSlug = slug,
                    onBack = { navController.popBackStack() },
                    onRelatedTopicClick = { newSlug ->
                        navController.navigate(Screen.TopicStudyDetail.createRoute(newSlug)) {
                            popUpTo(Screen.TopicStudyList.route)
                        }
                    }
                )
            }

            // v3.3.0 — Hadith Topic Study
            composable(Screen.HadithTopicStudyList.route) {
                com.islamichub.app.ui.screens.hadith_topic_study.HadithTopicStudyListScreen(
                    onBack = { navController.popBackStack() },
                    onTopicClick = { slug -> navController.navigate(Screen.HadithTopicStudyDetail.createRoute(slug)) }
                )
            }
            composable(
                route = Screen.HadithTopicStudyDetail.route,
                arguments = listOf(navArgument("slug") { type = NavType.StringType })
            ) { backStackEntry ->
                val slug = backStackEntry.arguments?.getString("slug").orEmpty()
                com.islamichub.app.ui.screens.hadith_topic_study.HadithTopicStudyDetailScreen(
                    container = container,
                    topicSlug = slug,
                    onBack = { navController.popBackStack() }
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

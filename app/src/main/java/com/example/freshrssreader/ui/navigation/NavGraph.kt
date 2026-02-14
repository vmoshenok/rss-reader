package com.example.freshrssreader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.freshrssreader.ui.screens.articles.ArticleListScreen
import com.example.freshrssreader.ui.screens.feedlist.FeedListScreen
import com.example.freshrssreader.ui.screens.login.LoginScreen
import com.example.freshrssreader.ui.screens.reader.ReaderScreen
import com.example.freshrssreader.ui.screens.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val LOGIN = "login"
    const val FEED_LIST = "feed_list"
    const val ARTICLE_LIST = "article_list/{streamId}/{title}"
    const val READER = "reader/{articleIndex}"
    const val SETTINGS = "settings"

    fun articleList(streamId: String, title: String): String {
        val encodedId = URLEncoder.encode(streamId, "UTF-8")
        val encodedTitle = URLEncoder.encode(title, "UTF-8")
        return "article_list/$encodedId/$encodedTitle"
    }

    fun reader(articleIndex: Int): String {
        return "reader/$articleIndex"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.FEED_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FEED_LIST) {
            FeedListScreen(
                onFeedClick = { streamId, title ->
                    navController.navigate(Routes.articleList(streamId, title))
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.ARTICLE_LIST,
            arguments = listOf(
                navArgument("streamId") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val streamId = URLDecoder.decode(
                backStackEntry.arguments?.getString("streamId") ?: "", "UTF-8"
            )
            val title = URLDecoder.decode(
                backStackEntry.arguments?.getString("title") ?: "", "UTF-8"
            )
            ArticleListScreen(
                streamId = streamId,
                title = title,
                onArticleClick = { index ->
                    navController.navigate(Routes.reader(index))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.READER,
            arguments = listOf(
                navArgument("articleIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val articleIndex = backStackEntry.arguments?.getInt("articleIndex") ?: 0
            ReaderScreen(
                articleIndex = articleIndex,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

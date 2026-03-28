package com.euysoo.engtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.euysoo.engtest.ui.screen.MainScreen
import com.euysoo.engtest.ui.screen.wordbook.MyWordBookDetailScreen
import com.euysoo.engtest.ui.screen.wordbook.MyWordBookScreen
import com.euysoo.engtest.ui.screen.wordmanage.WordManageScreen
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestScreen
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestViewModel
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestViewModelFactory
import com.euysoo.engtest.ui.screen.wordtest.WordTestScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModel
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModelFactory
import com.euysoo.engtest.ui.screen.wordtest.WordTestSelectScreen
import com.euysoo.engtest.ui.screen.records.RecordsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.euysoo.engtest.EngTestApplication

@Composable
fun EngTestNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Main
    ) {
        composable(NavRoutes.Main) {
            MainScreen(
                onNavigateToWordManage = { navController.navigate(NavRoutes.WordManage) },
                onNavigateToMyWordBook = { navController.navigate(NavRoutes.MyWordBook) },
                onNavigateToWordTest = { navController.navigate(NavRoutes.WordTestSelect) },
                onNavigateToRecords = { navController.navigate(NavRoutes.Records) },
            )
        }
        composable(NavRoutes.WordManage) {
            WordManageScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.MyWordBook) {
            MyWordBookScreen(
                onBack = { navController.popBackStack() },
                onOpenBook = { id -> navController.navigate(NavRoutes.myWordBookDetail(id)) },
            )
        }
        composable(
            route = NavRoutes.MyWordBookDetailRoute,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: -1L
            MyWordBookDetailScreen(
                bookId = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.WordTestSelect) {
            WordTestSelectScreen(
                onNavigateSelfTest = { difficulty ->
                    navController.navigate(NavRoutes.wordTest(difficulty))
                },
                onNavigateMultipleChoice = { difficulty ->
                    navController.navigate(NavRoutes.multipleChoiceTest(difficulty))
                },
                onBack = { navController.popBackStack() },
            )
        }
        composable(
            route = "${NavRoutes.WordTest}/{difficulty}",
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType; defaultValue = "all" })
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "all"
            val context = LocalContext.current
            val app = context.applicationContext as EngTestApplication
            val viewModel: WordTestViewModel = viewModel(
                factory = WordTestViewModelFactory(app, difficulty)
            )
            WordTestScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTestFinished = {
                    navController.popBackStack(NavRoutes.Main, false)
                }
            )
        }
        composable(
            route = "${NavRoutes.MultipleChoiceTest}/{difficulty}",
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType; defaultValue = "all" })
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "all"
            val context = LocalContext.current
            val app = context.applicationContext as EngTestApplication
            val viewModel: MultipleChoiceTestViewModel = viewModel(
                factory = MultipleChoiceTestViewModelFactory(app, difficulty)
            )
            MultipleChoiceTestScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTestFinished = {
                    navController.popBackStack(NavRoutes.Main, false)
                }
            )
        }
        composable(NavRoutes.Records) {
            RecordsScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}

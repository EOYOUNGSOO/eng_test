package com.euysoo.engtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.euysoo.engtest.ui.screen.MainScreen
import com.euysoo.engtest.ui.screen.wordmanage.WordManageScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestSelectScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModel
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModelFactory
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
                onNavigateToWordTest = { navController.navigate(NavRoutes.WordTestSelect) },
                onNavigateToRecords = { navController.navigate(NavRoutes.Records) },
            )
        }
        composable(NavRoutes.WordManage) {
            WordManageScreen(
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(NavRoutes.Main, false) }
            )
        }
        composable(NavRoutes.WordTestSelect) {
            WordTestSelectScreen(
                onSelectDifficulty = { difficulty ->
                    navController.navigate(NavRoutes.wordTest(difficulty))
                },
                onBack = { navController.popBackStack() },
                onHome = { navController.popBackStack(NavRoutes.Main, false) }
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
        composable(NavRoutes.Records) {
            RecordsScreen(
                onBack = { navController.popBackStack() },
                onBackToHome = { navController.popBackStack(NavRoutes.Main, false) }
            )
        }
    }
}

package com.example.engtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.engtest.ui.debug.LogViewerScreen
import com.example.engtest.ui.screen.MainScreen
import com.example.engtest.ui.screen.wordmanage.WordManageScreen
import com.example.engtest.ui.screen.wordtest.WordTestScreen
import com.example.engtest.ui.screen.wordtest.WordTestSelectScreen
import com.example.engtest.ui.screen.wordtest.WordTestViewModel
import com.example.engtest.ui.screen.wordtest.WordTestViewModelFactory
import com.example.engtest.ui.screen.records.RecordsScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.example.engtest.EngTestApplication

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
                onNavigateToLogViewer = { navController.navigate(NavRoutes.LogViewer) }
            )
        }
        composable(NavRoutes.LogViewer) {
            LogViewerScreen(
                onBackClick = { navController.popBackStack() }
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

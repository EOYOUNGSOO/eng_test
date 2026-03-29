package com.euysoo.engtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.euysoo.engtest.EngTestApplication
import com.euysoo.engtest.ui.screen.MainScreen
import com.euysoo.engtest.ui.screen.records.RecordsListScreen
import com.euysoo.engtest.ui.screen.records.RecordsResultDetailScreen
import com.euysoo.engtest.ui.screen.records.RecordsViewModel
import com.euysoo.engtest.ui.screen.records.RecordsViewModelFactory
import com.euysoo.engtest.ui.screen.wordbook.MyWordBookDetailScreen
import com.euysoo.engtest.ui.screen.wordbook.MyWordBookScreen
import com.euysoo.engtest.ui.screen.wordmanage.WordManageScreen
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestScreen
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestViewModel
import com.euysoo.engtest.ui.screen.wordtest.MultipleChoiceTestViewModelFactory
import com.euysoo.engtest.ui.screen.wordtest.WordTestScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestSelectScreen
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModel
import com.euysoo.engtest.ui.screen.wordtest.WordTestViewModelFactory

/**
 * 메인(시작 화면)까지 한 번에 되돌린다. 스택에 메인이 없는 비정상 상태에서는
 * 빈 스택으로 `popBackStack()`만 호출하면 Activity가 종료될 수 있어,
 * 실패 시 메인으로 다시 진입해 스택을 복구한다.
 */
private fun NavHostController.popBackToMainOrNavigate() {
    if (!popBackStack(NavRoutes.MAIN, inclusive = false)) {
        val mainId = graph.findNode(NavRoutes.MAIN)?.id ?: graph.startDestinationId
        navigate(
            NavRoutes.MAIN,
            NavOptions.Builder()
                .setPopUpTo(mainId, true)
                .setLaunchSingleTop(true)
                .build(),
        )
    }
}

/**
 * 앱 전체 [NavHost]. 메인·단어 관리·단어장·테스트·기록 화면을 연결한다.
 */
@Composable
fun EngTestNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN,
    ) {
        composable(NavRoutes.MAIN) {
            MainScreen(
                onNavigateToWordManage = { navController.navigate(NavRoutes.WORD_MANAGE) },
                onNavigateToMyWordBook = { navController.navigate(NavRoutes.MY_WORD_BOOK) },
                onNavigateToWordTest = { navController.navigate(NavRoutes.WORD_TEST_SELECT) },
                onNavigateToRecords = { navController.navigate(NavRoutes.RECORDS) },
            )
        }
        composable(NavRoutes.WORD_MANAGE) {
            WordManageScreen(
                onBack = { navController.popBackToMainOrNavigate() },
            )
        }
        composable(NavRoutes.MY_WORD_BOOK) {
            MyWordBookScreen(
                onBack = { navController.popBackToMainOrNavigate() },
                onOpenBook = { id -> navController.navigate(NavRoutes.myWordBookDetail(id)) },
            )
        }
        composable(
            route = NavRoutes.MY_WORD_BOOK_DETAIL_ROUTE,
            arguments = listOf(navArgument("bookId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("bookId")?.toLongOrNull() ?: -1L
            MyWordBookDetailScreen(
                bookId = id,
                onBack = { navController.popBackStack() },
            )
        }
        composable(NavRoutes.WORD_TEST_SELECT) {
            WordTestSelectScreen(
                onNavigateSelfTest = { difficulty ->
                    navController.navigate(NavRoutes.wordTest(difficulty))
                },
                onNavigateMultipleChoice = { difficulty ->
                    navController.navigate(NavRoutes.multipleChoiceTest(difficulty))
                },
                onBack = { navController.popBackToMainOrNavigate() },
            )
        }
        composable(
            route = "${NavRoutes.WORD_TEST}/{difficulty}",
            arguments =
                listOf(
                    navArgument("difficulty") {
                        type = NavType.StringType
                        defaultValue = "all"
                    },
                ),
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "all"
            val context = LocalContext.current
            val app = context.applicationContext as EngTestApplication
            val viewModel: WordTestViewModel =
                viewModel(
                    factory = WordTestViewModelFactory(app.appContainer, difficulty),
                )
            WordTestScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTestFinished = {
                    navController.popBackStack(NavRoutes.MAIN, false)
                },
            )
        }
        composable(
            route = "${NavRoutes.MULTIPLE_CHOICE_TEST}/{difficulty}",
            arguments =
                listOf(
                    navArgument("difficulty") {
                        type = NavType.StringType
                        defaultValue = "all"
                    },
                ),
        ) { backStackEntry ->
            val difficulty = backStackEntry.arguments?.getString("difficulty") ?: "all"
            val context = LocalContext.current
            val app = context.applicationContext as EngTestApplication
            val viewModel: MultipleChoiceTestViewModel =
                viewModel(
                    factory = MultipleChoiceTestViewModelFactory(app.appContainer, difficulty),
                )
            MultipleChoiceTestScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTestFinished = {
                    navController.popBackStack(NavRoutes.MAIN, false)
                },
            )
        }
        composable(NavRoutes.RECORDS) {
            RecordsListScreen(
                onBack = { navController.popBackToMainOrNavigate() },
                onOpenResultDetail = { id ->
                    navController.navigate(NavRoutes.recordsDetail(id))
                },
            )
        }
        composable(
            route = NavRoutes.RECORDS_DETAIL_ROUTE,
            arguments = listOf(navArgument("resultId") { type = NavType.LongType }),
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getLong("resultId") ?: 0L
            val context = LocalContext.current
            val app = context.applicationContext as EngTestApplication
            val listEntry =
                runCatching { navController.getBackStackEntry(NavRoutes.RECORDS) }.getOrNull()
            val recordsViewModel: RecordsViewModel =
                viewModel(
                    viewModelStoreOwner = listEntry ?: backStackEntry,
                    factory = RecordsViewModelFactory(app.appContainer),
                )
            RecordsResultDetailScreen(
                resultId = resultId,
                viewModel = recordsViewModel,
                onBack = {
                    recordsViewModel.clearSelection()
                    navController.popBackStack()
                },
            )
        }
    }
}

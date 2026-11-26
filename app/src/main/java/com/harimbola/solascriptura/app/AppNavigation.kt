package com.harimbola.solascriptura.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.R
import com.harimbola.solascriptura.feature.account.AccountScreen
import com.harimbola.solascriptura.feature.dailychallenge.DailyChallengeScreen
import com.harimbola.solascriptura.feature.dialogue.DialogueScreen
import com.harimbola.solascriptura.feature.duel.VerseDuelScreen
import com.harimbola.solascriptura.feature.home.HomeScreen
import com.harimbola.solascriptura.feature.investigation.InvestigationScreen
import com.harimbola.solascriptura.feature.notebook.NotebookScreen
import com.harimbola.solascriptura.feature.onboarding.OnboardingScreen
import com.harimbola.solascriptura.feature.progress.ProgressScreen
import com.harimbola.solascriptura.feature.settings.SettingsScreen
import com.harimbola.solascriptura.feature.studies.StudiesScreen
import com.harimbola.solascriptura.feature.studies.StudyDetailScreen
import com.harimbola.solascriptura.feature.truthmap.TruthTreeScreen
import kotlinx.serialization.Serializable

@Serializable data object OnboardingRoute
@Serializable data object HomeRoute
@Serializable data class StudiesRoute(val categoryId: String? = null)
@Serializable data class StudyDetailRoute(val studyId: String)
@Serializable data class DialogueRoute(val studyId: String)
@Serializable data class VerseDuelRoute(val studyId: String)
@Serializable data class InvestigationRoute(val studyId: String)
@Serializable data object TruthTreeRoute
@Serializable data object NotebookRoute
@Serializable data object DailyChallengeRoute
@Serializable data object ProgressRoute
@Serializable data object AccountRoute
@Serializable data object SettingsRoute

private data class TopLevelDestination(
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String,
    val route: Any
)

private val topLevelDestinations = listOf(
    TopLevelDestination(R.string.nav_home, Icons.Rounded.Home, "nav_home", HomeRoute),
    TopLevelDestination(R.string.nav_studies, Icons.AutoMirrored.Rounded.MenuBook, "nav_studies", StudiesRoute()),
    TopLevelDestination(R.string.nav_truth_tree, Icons.Rounded.AccountTree, "nav_truth_tree", TruthTreeRoute),
    TopLevelDestination(R.string.nav_notebook, Icons.Rounded.EditNote, "nav_notebook", NotebookRoute),
    TopLevelDestination(R.string.nav_settings, Icons.Rounded.Settings, "nav_settings", SettingsRoute)
)

@Composable
fun SolaScripturaNavHost(
    onboardingCompleted: Boolean
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val showTopLevelChrome = currentDestination != null && (
        currentDestination.hasRoute<HomeRoute>() ||
            currentDestination.hasRoute<StudiesRoute>() ||
            currentDestination.hasRoute<TruthTreeRoute>() ||
            currentDestination.hasRoute<NotebookRoute>() ||
            currentDestination.hasRoute<SettingsRoute>()
        )

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showTopLevelChrome && !useRail) {
                    NavigationBar {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(dest.route::class)
                            } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showTopLevelChrome && useRail) {
                    NavigationRail {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.hasRoute(dest.route::class)
                            } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = { Icon(dest.icon, contentDescription = stringResource(dest.labelRes)) },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = if (onboardingCompleted) HomeRoute else OnboardingRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<OnboardingRoute> {
                        OnboardingScreen(
                            onFinished = {
                                navController.navigate(HomeRoute) {
                                    popUpTo<OnboardingRoute> { inclusive = true }
                                }
                            }
                        )
                    }

                    composable<HomeRoute> {
                        HomeScreen(
                            onOpenStudyDetail = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            },
                            onResumeDialogue = { studyId ->
                                navController.navigate(DialogueRoute(studyId))
                            },
                            onOpenDailyChallenge = {
                                navController.navigate(DailyChallengeRoute)
                            },
                            onOpenProgress = {
                                navController.navigate(ProgressRoute)
                            },
                            onOpenNotebook = {
                                navigateTopLevel(NotebookRoute)
                            },
                            onOpenCategoryStudies = { categoryId ->
                                navController.navigate(StudiesRoute(categoryId = categoryId))
                            }
                        )
                    }

                    composable<StudiesRoute> { entry ->
                        val route = entry.toRoute<StudiesRoute>()
                        StudiesScreen(
                            initialCategoryId = route.categoryId,
                            onOpenStudyDetail = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            }
                        )
                    }

                    composable<StudyDetailRoute> { entry ->
                        val route = entry.toRoute<StudyDetailRoute>()
                        StudyDetailScreen(
                            studyId = route.studyId,
                            onBack = { navController.popBackStack() },
                            onStartDialogue = { studyId ->
                                navController.navigate(DialogueRoute(studyId))
                            },
                            onOpenVerseDuel = { studyId ->
                                navController.navigate(VerseDuelRoute(studyId))
                            },
                            onOpenInvestigation = { studyId ->
                                navController.navigate(InvestigationRoute(studyId))
                            },
                            onOpenRelatedStudy = { relatedId ->
                                navController.navigate(StudyDetailRoute(relatedId))
                            }
                        )
                    }

                    composable<DialogueRoute> {
                        DialogueScreen(
                            onBack = { navController.popBackStack() },
                            onOpenRelatedStudy = { relatedId ->
                                navController.navigate(StudyDetailRoute(relatedId))
                            }
                        )
                    }

                    composable<VerseDuelRoute> {
                        VerseDuelScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<InvestigationRoute> {
                        InvestigationScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<TruthTreeRoute> {
                        TruthTreeScreen(
                            onOpenStudyDetail = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            }
                        )
                    }

                    composable<NotebookRoute> {
                        NotebookScreen(
                            onOpenStudyDetail = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            }
                        )
                    }

                    composable<DailyChallengeRoute> {
                        DailyChallengeScreen(
                            onBack = { navController.popBackStack() },
                            onOpenStudy = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            }
                        )
                    }

                    composable<ProgressRoute> {
                        ProgressScreen(
                            onBack = { navController.popBackStack() },
                            onOpenStudyDetail = { studyId ->
                                navController.navigate(StudyDetailRoute(studyId))
                            }
                        )
                    }

                    composable<AccountRoute> {
                        AccountScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<SettingsRoute> {
                        SettingsScreen(
                            onOpenAccount = {
                                navController.navigate(AccountRoute)
                            },
                            onOpenNotebookExport = {
                                navigateTopLevel(NotebookRoute)
                            }
                        )
                    }
                }
            }
        }
    }
}

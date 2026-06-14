package com.khata.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.khata.app.presentation.onboarding.OnboardingScreen
import com.khata.app.presentation.dashboard.DashboardScreen
import com.khata.app.presentation.expense.AddExpenseScreen
import com.khata.app.presentation.meal.MealTrackerScreen
import com.khata.app.presentation.settlement.SettlementScreen
import com.khata.app.presentation.group.CreateGroupScreen
import com.khata.app.presentation.group.GroupDetailScreen
import com.khata.app.presentation.group.AddMemberScreen
import com.khata.app.presentation.settings.SettingsScreen
import com.khata.app.presentation.expense.ExpenseListScreen

/**
 * Main Navigation Graph for Khata.
 */
@Composable
fun KhataNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Onboarding.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController)
        }
        
        composable(Screen.Auth.route) {
            // AuthScreen(navController)
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController)
        }
        
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            AddExpenseScreen(navController)
        }
        
        composable(
            route = Screen.ExpenseDetail.route,
            arguments = listOf(navArgument("expenseId") { type = NavType.StringType })
        ) {
            // ExpenseDetailScreen(navController)
        }
        
        composable(
            route = Screen.ExpenseList.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            ExpenseListScreen(navController)
        }
        
        composable(
            route = Screen.MealTracker.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            MealTrackerScreen(navController)
        }
        
        composable(
            route = Screen.MealAnalytics.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            // MealAnalyticsScreen(navController)
        }
        
        composable(
            route = Screen.Settlement.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            SettlementScreen(navController)
        }
        
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(navController)
        }
        
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            GroupDetailScreen(navController)
        }

        composable(
            route = Screen.AddMember.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) {
            AddMemberScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
    }
}

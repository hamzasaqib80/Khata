package com.khata.app.core.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Auth : Screen("auth")
    data object Dashboard : Screen("dashboard")
    
    data object AddExpense : Screen("add_expense/{groupId}") {
        fun createRoute(groupId: String) = "add_expense/$groupId"
    }
    
    data object ExpenseDetail : Screen("expense_detail/{expenseId}") {
        fun createRoute(expenseId: String) = "expense_detail/$expenseId"
    }
    
    data object ExpenseList : Screen("expense_list/{groupId}") {
        fun createRoute(groupId: String) = "expense_list/$groupId"
    }
    
    data object MealTracker : Screen("meal_tracker/{groupId}") {
        fun createRoute(groupId: String) = "meal_tracker/$groupId"
    }
    
    data object MealAnalytics : Screen("meal_analytics/{groupId}") {
        fun createRoute(groupId: String) = "meal_analytics/$groupId"
    }
    
    data object Settlement : Screen("settlement/{groupId}") {
        fun createRoute(groupId: String) = "settlement/$groupId"
    }
    
    data object CreateGroup : Screen("create_group")
    
    data object GroupDetail : Screen("group_detail/{groupId}") {
        fun createRoute(groupId: String) = "group_detail/$groupId"
    }

    data object AddMember : Screen("add_member/{groupId}") {
        fun createRoute(groupId: String) = "add_member/$groupId"
    }
    
    data object Settings : Screen("settings")
}

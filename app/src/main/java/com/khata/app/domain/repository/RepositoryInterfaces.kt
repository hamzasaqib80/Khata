package com.khata.app.domain.repository

import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface UserRepository {
    fun observeAllUsers(): Flow<Result<List<User>>>
    fun observeCurrentUser(): Flow<Result<User?>>
    suspend fun saveUser(user: User): Result<Unit>
    suspend fun getUserById(userId: String): Result<User?>
}

interface GroupRepository {
    fun observeAllGroups(): Flow<Result<List<Group>>>
    fun observeGroupById(groupId: String): Flow<Result<Group?>>
    suspend fun createGroup(group: Group): Result<Unit>
    suspend fun getGroupById(groupId: String): Result<Group?>
    suspend fun updateGroup(group: Group): Result<Unit>
    suspend fun addMemberToGroup(groupId: String, user: User): Result<Unit>
}

interface ExpenseRepository {
    fun observeExpensesByGroup(groupId: String): Flow<Result<List<Expense>>>
    fun observeRecentExpenses(groupId: String, limit: Int): Flow<Result<List<Expense>>>
    /** Reactive balance flow — re-emits whenever any expense in the group changes. */
    fun observeNetBalances(groupId: String): Flow<Result<List<Balance>>>
    suspend fun addExpense(expense: Expense): Result<Unit>
    suspend fun deleteExpense(expenseId: String): Result<Unit>
    suspend fun getNetBalances(groupId: String): Result<List<Balance>>
}

interface MealRepository {
    fun observeMealPlan(groupId: String, month: Int, year: Int): Flow<Result<MealPlan?>>
    fun observeMealLogs(mealPlanId: String): Flow<Result<List<MealLog>>>
    suspend fun upsertMealLog(log: MealLog, mealPlanId: String): Result<Unit>
    suspend fun saveMealPlan(plan: MealPlan): Result<Unit>
    suspend fun getMealCounts(mealPlanId: String): Result<Map<String, UserMealBreakdown>>
}

interface SettlementRepository {
    fun observeSettlements(groupId: String): Flow<Result<List<SimplifiedTransaction>>>
    suspend fun recordSettlement(transaction: SimplifiedTransaction, groupId: String): Result<Unit>
}

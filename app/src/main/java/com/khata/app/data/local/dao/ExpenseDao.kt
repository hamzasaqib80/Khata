package com.khata.app.data.local.dao

import androidx.room.*
import com.khata.app.data.local.entity.ExpenseEntity
import com.khata.app.data.local.entity.ExpenseParticipantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC")
    fun observeExpensesByGroup(groupId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun observeExpensesByGroupAndDateRange(
        groupId: String, startDate: Long, endDate: Long
    ): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY date DESC LIMIT :limit")
    fun observeRecentExpenses(groupId: String, limit: Int): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expense_participants WHERE expenseId = :expenseId")
    fun observeParticipantsForExpense(expenseId: String): Flow<List<ExpenseParticipantEntity>>

    @Query("SELECT * FROM expenses WHERE payerId = :userId OR id IN (SELECT expenseId FROM expense_participants WHERE userId = :userId) ORDER BY date DESC")
    fun observeExpensesForUser(userId: String): Flow<List<ExpenseEntity>>

    // ─── Enriched query: expense + payer name in one shot ───────────────────
    @Query("""
        SELECT e.*, u.name AS payerName
        FROM expenses e
        INNER JOIN users u ON e.payerId = u.id
        WHERE e.groupId = :groupId
        ORDER BY e.date DESC
    """)
    fun observeExpensesWithPayerName(groupId: String): Flow<List<ExpenseWithPayer>>

    @Query("""
        SELECT e.*, u.name AS payerName
        FROM expenses e
        INNER JOIN users u ON e.payerId = u.id
        WHERE e.groupId = :groupId
        ORDER BY e.date DESC
        LIMIT :limit
    """)
    fun observeRecentExpensesWithPayerName(groupId: String, limit: Int): Flow<List<ExpenseWithPayer>>

    // ─── Aggregation queries (reactive Flow — Room re-emits on table change) ─
    @Query("""
        SELECT ep.userId, SUM(CAST(ep.shareAmount AS REAL)) as totalOwed
        FROM expense_participants ep
        INNER JOIN expenses e ON ep.expenseId = e.id
        WHERE e.groupId = :groupId
        GROUP BY ep.userId
    """)
    fun observeTotalOwedPerUser(groupId: String): Flow<List<UserOwedAmount>>

    @Query("""
        SELECT payerId, SUM(CAST(amount AS REAL)) as totalPaid
        FROM expenses
        WHERE groupId = :groupId
        GROUP BY payerId
    """)
    fun observeTotalPaidPerUser(groupId: String): Flow<List<UserPaidAmount>>

    // ─── Snapshot aggregation (kept for backward compat) ────────────────────
    @Query("SELECT SUM(CAST(amount AS REAL)) FROM expenses WHERE groupId = :groupId")
    suspend fun getTotalExpenseAmountForGroup(groupId: String): String?

    @Query("""
        SELECT ep.userId, SUM(CAST(ep.shareAmount AS REAL)) as totalOwed
        FROM expense_participants ep
        INNER JOIN expenses e ON ep.expenseId = e.id
        WHERE e.groupId = :groupId
        GROUP BY ep.userId
    """)
    suspend fun getTotalOwedPerUser(groupId: String): List<UserOwedAmount>

    @Query("""
        SELECT payerId, SUM(CAST(amount AS REAL)) as totalPaid
        FROM expenses
        WHERE groupId = :groupId
        GROUP BY payerId
    """)
    suspend fun getTotalPaidPerUser(groupId: String): List<UserPaidAmount>

    // ─── Search ──────────────────────────────────────────────────────────────
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND description LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchExpenses(groupId: String, query: String): Flow<List<ExpenseEntity>>

    // ─── Write operations ────────────────────────────────────────────────────
    @Transaction
    suspend fun insertExpenseWithParticipants(
        expense: ExpenseEntity,
        participants: List<ExpenseParticipantEntity>
    ) {
        insertExpense(expense)
        insertParticipants(participants)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<ExpenseParticipantEntity>)

    @Transaction
    suspend fun deleteExpenseWithParticipants(expenseId: String) {
        deleteParticipantsForExpense(expenseId)
        deleteExpense(expenseId)
    }

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpense(expenseId: String)

    @Query("DELETE FROM expense_participants WHERE expenseId = :expenseId")
    suspend fun deleteParticipantsForExpense(expenseId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ExpenseParticipantEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)
}

// ─── Projection for JOIN result ───────────────────────────────────────────────
data class ExpenseWithPayer(
    val id: String,
    val groupId: String,
    val payerId: String,
    val payerName: String,
    val description: String,
    val amount: String,
    val currency: String,
    val splitType: String,
    val category: String,
    val receiptNote: String?,
    val date: Long,
    val createdAt: Long
)

data class UserOwedAmount(
    val userId: String,
    val totalOwed: Double
)

data class UserPaidAmount(
    val payerId: String,
    val totalPaid: Double
)

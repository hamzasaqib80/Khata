package com.khata.app.data.repository

import com.khata.app.core.utils.Result
import com.khata.app.core.utils.asResult
import com.khata.app.core.utils.safeCall
import com.khata.app.data.local.dao.ExpenseDao
import com.khata.app.data.local.dao.ExpenseWithPayer
import com.khata.app.data.local.dao.UserDao
import com.khata.app.data.local.dao.SettlementDao
import com.khata.app.data.local.entity.ExpenseEntity
import com.khata.app.data.local.entity.ExpenseParticipantEntity
import com.khata.app.data.local.mapper.toDomain
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val userDao: UserDao,
    private val settlementDao: SettlementDao
) : ExpenseRepository {

    // ─── Observe expenses with enriched payer name ───────────────────────────
    override fun observeExpensesByGroup(groupId: String): Flow<Result<List<Expense>>> {
        return expenseDao.observeExpensesWithPayerName(groupId)
            .map { rows -> rows.map { it.toDomain() } }
            .asResult()
    }

    override fun observeRecentExpenses(groupId: String, limit: Int): Flow<Result<List<Expense>>> {
        return expenseDao.observeRecentExpensesWithPayerName(groupId, limit)
            .map { rows -> rows.map { it.toDomain() } }
            .asResult()
    }

    // ─── Reactive balance observation ────────────────────────────────────────
    override fun observeNetBalances(groupId: String): Flow<Result<List<Balance>>> {
        return combine(
            expenseDao.observeTotalPaidPerUser(groupId),
            expenseDao.observeTotalOwedPerUser(groupId),
            settlementDao.observeTotalPaidSettlementsPerUser(groupId),
            settlementDao.observeTotalReceivedSettlementsPerUser(groupId)
        ) { paidRows, owedRows, paidSetRows, receivedSetRows ->
            val paidMap = paidRows.associate { it.payerId to BigDecimal(it.totalPaid) }
            val owedMap = owedRows.associate { it.userId to BigDecimal(it.totalOwed) }
            val paidSetMap = paidSetRows.associate { it.userId to BigDecimal(it.totalAmount) }
            val receivedSetMap = receivedSetRows.associate { it.userId to BigDecimal(it.totalAmount) }

            // Collect all unique userIds mentioned in either map
            val allUserIds = (paidMap.keys + owedMap.keys + paidSetMap.keys + receivedSetMap.keys).toSet()

            allUserIds.map { userId ->
                val paidEx = paidMap[userId] ?: BigDecimal.ZERO
                val owedEx = owedMap[userId] ?: BigDecimal.ZERO
                
                val paidSet = paidSetMap[userId] ?: BigDecimal.ZERO
                val receivedSet = receivedSetMap[userId] ?: BigDecimal.ZERO

                val totalActuallyPaid = paidEx + paidSet
                val totalActuallyOwed = owedEx + receivedSet
                
                Balance(
                    user = User(userId, "", "", null, null, false), // name resolved in VM
                    totalPaid = totalActuallyPaid,
                    totalOwed = totalActuallyOwed,
                    netBalance = totalActuallyPaid - totalActuallyOwed
                )
            }
        }.asResult()
    }

    override suspend fun addExpense(expense: Expense): Result<Unit> = safeCall {
        val expenseEntity = expense.toEntity()
        val participantEntities = expense.participants.map { it.toEntity(expense.id) }
        expenseDao.insertExpenseWithParticipants(expenseEntity, participantEntities)
    }

    override suspend fun deleteExpense(expenseId: String): Result<Unit> = safeCall {
        expenseDao.deleteExpenseWithParticipants(expenseId)
    }

    override suspend fun getNetBalances(groupId: String): Result<List<Balance>> = safeCall {
        val users = userDao.getAllUsersSnapshot()
        val totalPaidEx = expenseDao.getTotalPaidPerUser(groupId).associate { it.payerId to BigDecimal(it.totalPaid) }
        val totalOwedEx = expenseDao.getTotalOwedPerUser(groupId).associate { it.userId to BigDecimal(it.totalOwed) }

        val totalPaidSet = settlementDao.getTotalPaidSettlementsPerUser(groupId).associate { it.userId to BigDecimal(it.totalAmount) }
        val totalReceivedSet = settlementDao.getTotalReceivedSettlementsPerUser(groupId).associate { it.userId to BigDecimal(it.totalAmount) }

        users.map { userEntity ->
            val paidEx = totalPaidEx[userEntity.id] ?: BigDecimal.ZERO
            val owedEx = totalOwedEx[userEntity.id] ?: BigDecimal.ZERO

            val paidSet = totalPaidSet[userEntity.id] ?: BigDecimal.ZERO
            val receivedSet = totalReceivedSet[userEntity.id] ?: BigDecimal.ZERO

            val totalActuallyPaid = paidEx + paidSet
            val totalActuallyOwed = owedEx + receivedSet

            Balance(
                user = userEntity.toDomain(),
                totalPaid = totalActuallyPaid,
                totalOwed = totalActuallyOwed,
                netBalance = totalActuallyPaid - totalActuallyOwed
            )
        }
    }

    // ─── Private mappers ─────────────────────────────────────────────────────

    /** Maps an enriched JOIN row to a full domain Expense with real payer name. */
    private fun ExpenseWithPayer.toDomain(): Expense {
        val localDate = try {
            Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            LocalDate.now()
        }
        return Expense(
            id = id,
            groupId = groupId,
            payer = User(payerId, payerName, "", null, null, false),
            description = description,
            amount = BigDecimal(amount),
            currency = Currency.valueOf(currency),
            splitType = SplitType.valueOf(splitType),
            category = ExpenseCategory.valueOf(category),
            participants = emptyList(),
            date = localDate,
            receiptNote = receiptNote
        )
    }

    private fun Expense.toEntity() = ExpenseEntity(
        id = id,
        groupId = groupId,
        payerId = payer.id,
        description = description,
        amount = amount.toPlainString(),
        currency = currency.name,
        splitType = splitType.name,
        category = category.name,
        receiptNote = receiptNote,
        date = System.currentTimeMillis(),
        createdAt = System.currentTimeMillis()
    )

    private fun ExpenseSplit.toEntity(expenseId: String) = ExpenseParticipantEntity(
        id = java.util.UUID.randomUUID().toString(),
        expenseId = expenseId,
        userId = user.id,
        shareAmount = shareAmount.toPlainString(),
        sharePercentage = sharePercentage?.toPlainString()
    )
}

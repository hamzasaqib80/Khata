package com.khata.app.domain.usecase

import com.khata.app.core.utils.Result
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.ExpenseRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: Expense): Result<Unit> {
        if (expense.amount <= BigDecimal.ZERO) {
            return Result.Error(IllegalArgumentException(), "Amount must be greater than zero")
        }
        if (expense.participants.isEmpty()) {
            return Result.Error(IllegalArgumentException(), "At least one participant is required")
        }

        // Validate split sums
        val totalSplit = expense.participants.sumOf { it.shareAmount }
        if ((totalSplit - expense.amount).abs() > BigDecimal("0.01")) {
            return Result.Error(IllegalArgumentException(), "Split amounts must sum up to the total expense")
        }

        if (expense.splitType == SplitType.PERCENTAGE) {
            val totalPercent = expense.participants.sumOf { it.sharePercentage ?: BigDecimal.ZERO }
            if ((totalPercent - BigDecimal("100")).abs() > BigDecimal("0.01")) {
                return Result.Error(IllegalArgumentException(), "Percentages must sum to 100%")
            }
        }

        return repository.addExpense(expense)
    }
}

class GetGroupBalancesUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(groupId: String): Result<List<Balance>> {
        return repository.getNetBalances(groupId)
    }
}

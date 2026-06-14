package com.khata.app.domain.usecase

import com.khata.app.core.utils.Result
import com.khata.app.domain.algorithm.DebtSimplifier
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.ExpenseRepository
import javax.inject.Inject

class SimplifyDebtsUseCase @Inject constructor(
    private val repository: ExpenseRepository,
    private val simplifier: DebtSimplifier
) {
    suspend operator fun invoke(groupId: String, currency: Currency): Result<List<SimplifiedTransaction>> {
        return when (val balancesResult = repository.getNetBalances(groupId)) {
            is Result.Success -> {
                val transactions = simplifier.simplify(balancesResult.data, currency)
                Result.Success(transactions)
            }
            is Result.Error -> balancesResult
            is Result.Loading -> Result.Loading
        }
    }
}

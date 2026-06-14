package com.khata.app.data.repository

import com.khata.app.core.utils.Result
import com.khata.app.core.utils.asResult
import com.khata.app.core.utils.safeCall
import com.khata.app.data.local.dao.SettlementDao
import com.khata.app.data.local.entity.SettlementEntity
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.SettlementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.util.UUID
import javax.inject.Inject

class SettlementRepositoryImpl @Inject constructor(
    private val settlementDao: SettlementDao
) : SettlementRepository {

    override fun observeSettlements(groupId: String): Flow<Result<List<SimplifiedTransaction>>> {
        return settlementDao.observeSettlementsForGroup(groupId).map { entities ->
            entities.map { it.toDomain() }
        }.asResult()
    }

    override suspend fun recordSettlement(
        transaction: SimplifiedTransaction, 
        groupId: String
    ): Result<Unit> = safeCall {
        val settlementEntity = SettlementEntity(
            id = UUID.randomUUID().toString(),
            groupId = groupId,
            payerUserId = transaction.from.id,
            receiverUserId = transaction.to.id,
            amount = transaction.amount.toPlainString(),
            currency = transaction.currency.name,
            paymentMethod = "CASH", // Default
            transactionReference = null,
            settledAt = System.currentTimeMillis(),
            note = "Settled via automated engine"
        )
        settlementDao.insertSettlement(settlementEntity)
    }

    private fun SettlementEntity.toDomain() = SimplifiedTransaction(
        from = User(payerUserId, "", "", null, null, false), // Stub user
        to = User(receiverUserId, "", "", null, null, false), // Stub user
        amount = BigDecimal(amount),
        currency = Currency.valueOf(currency)
    )
}

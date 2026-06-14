package com.khata.app.domain.algorithm

import com.khata.app.domain.model.Balance
import com.khata.app.domain.model.Currency
import com.khata.app.domain.model.SimplifiedTransaction
import com.khata.app.domain.model.User
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.PriorityQueue
import javax.inject.Inject

/**
 * DebtSimplifier — Optimal Debt Settlement Algorithm
 */
class DebtSimplifier @Inject constructor() {

    /**
     * Simplifies a list of net balances into the minimum number of transactions.
     * Uses a greedy approach with a Max-Heap for creditors and a Min-Heap for debtors.
     */
    fun simplify(balances: List<Balance>, currency: Currency): List<SimplifiedTransaction> {
        if (balances.isEmpty()) return emptyList()

        // Filter out tiny amounts (dust) and separate into creditors and debtors
        val userMap = balances.associate { it.user.id to it.user }
        
        // Priority queues: 
        // creditors (max-heap): highest positive balance first
        val creditors = PriorityQueue<Pair<String, BigDecimal>> { a, b -> 
            b.second.compareTo(a.second) 
        }
        
        // debtors (min-heap/max-heap of absolute value): most negative balance first
        val debtors = PriorityQueue<Pair<String, BigDecimal>> { a, b -> 
            a.second.compareTo(b.second) 
        }

        balances.forEach { balance ->
            val net = balance.netBalance.setScale(2, RoundingMode.HALF_UP)
            when {
                net > BigDecimal("0.01") -> creditors.add(balance.user.id to net)
                net < BigDecimal("-0.01") -> debtors.add(balance.user.id to net)
            }
        }

        val transactions = mutableListOf<SimplifiedTransaction>()

        while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
            settleOnePair(creditors, debtors, userMap, currency, transactions)
        }

        return transactions
    }

    private fun settleOnePair(
        creditors: PriorityQueue<Pair<String, BigDecimal>>,
        debtors: PriorityQueue<Pair<String, BigDecimal>>,
        userMap: Map<String, User>,
        currency: Currency,
        result: MutableList<SimplifiedTransaction>
    ) {
        val creditor = creditors.poll() ?: return
        val debtor = debtors.poll() ?: return

        val creditAmount = creditor.second
        val debitAmount = debtor.second.abs()

        val settlementAmount = creditAmount.min(debitAmount)

        val fromUser = userMap[debtor.first] ?: return
        val toUser = userMap[creditor.first] ?: return

        result.add(
            SimplifiedTransaction(
                from = fromUser,
                to = toUser,
                amount = settlementAmount,
                currency = currency
            )
        )

        val remainingCredit = creditAmount - settlementAmount
        val remainingDebit = (debitAmount - settlementAmount).negate()

        if (remainingCredit > BigDecimal("0.01")) {
            creditors.add(creditor.first to remainingCredit)
        }
        if (remainingDebit < BigDecimal("-0.01")) {
            debtors.add(debtor.first to remainingDebit)
        }
    }
}

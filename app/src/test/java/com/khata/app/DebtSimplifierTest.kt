package com.khata.app.test

import com.khata.app.domain.algorithm.DebtSimplifier
import com.khata.app.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

/**
 * Unit tests for DebtSimplifier algorithm.
 * Covers: correctness, minimality, edge cases, and rounding.
 */
class DebtSimplifierTest {

    private lateinit var simplifier: DebtSimplifier

    @Before
    fun setup() {
        simplifier = DebtSimplifier()
    }

    private fun user(id: String, name: String = id) = User(id, name, "#1B6B5C", null, false)
    private fun balance(user: User, paid: String, owed: String) = Balance(
        user = user,
        totalPaid = BigDecimal(paid),
        totalOwed = BigDecimal(owed),
        netBalance = BigDecimal(paid) - BigDecimal(owed)
    )

    /** Test: 2 people, A paid B owes → exactly 1 transaction */
    @Test
    fun `two people simple debt - one transaction`() {
        val alice = user("alice")
        val bob = user("bob")
        val balances = listOf(
            balance(alice, "1000", "0"),
            balance(bob, "0", "1000")
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertEquals(1, result.size)
        assertEquals("bob", result[0].from.id)
        assertEquals("alice", result[0].to.id)
        assertEquals(BigDecimal("1000.00"), result[0].amount)
    }

    /** Test: 3 people circular debt → 2 transactions (not 3) */
    @Test
    fun `three people circular - minimal transactions`() {
        val a = user("a", "Ali")
        val b = user("b", "Hassan")
        val c = user("c", "Usman")
        // Ali paid 300, owes 100 (net +200)
        // Hassan paid 100, owes 200 (net -100)
        // Usman paid 50, owes 150 (net -100)
        val balances = listOf(
            balance(a, "300", "100"),
            balance(b, "100", "200"),
            balance(c, "50", "150")
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertTrue("Expected 2 or fewer transactions", result.size <= 2)
        // Verify all debts are settled by checking sum from debtors equals sum to creditors
        val totalSettled = result.sumOf { it.amount }
        assertEquals(BigDecimal("200.00"), totalSettled.setScale(2, java.math.RoundingMode.HALF_UP))
    }

    /** Test: All balances zero → 0 transactions */
    @Test
    fun `all balances zero - no transactions`() {
        val balances = listOf(
            balance(user("a"), "500", "500"),
            balance(user("b"), "200", "200")
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertEquals(0, result.size)
    }

    /** Test: 4 people complex mixed debt */
    @Test
    fun `four people complex - minimized transactions`() {
        val people = (1..4).map { user("u$it", "User$it") }
        val balances = listOf(
            balance(people[0], "3000", "1000"),  // net: +2000
            balance(people[1], "500", "1500"),   // net: -1000
            balance(people[2], "200", "1200"),   // net: -1000
            balance(people[3], "300", "300")     // net: 0
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertTrue("Should have at most 3 transactions for 4 people", result.size <= 3)
    }

    /** Test: empty input → empty result */
    @Test
    fun `empty balances - no transactions`() {
        val result = simplifier.simplify(emptyList(), Currency.PKR)
        assertEquals(0, result.size)
    }

    /** Test: Rounding edge case with 0.01 PKR residual */
    @Test
    fun `rounding residual - handled correctly`() {
        val a = user("a")
        val b = user("b")
        val balances = listOf(
            balance(a, "100.005", "0"),
            balance(b, "0", "100.005")
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertEquals(1, result.size)
    }

    /** Test: Single creditor multiple debtors */
    @Test
    fun `single creditor multiple debtors`() {
        val creditor = user("creditor")
        val d1 = user("d1")
        val d2 = user("d2")
        val d3 = user("d3")
        // creditor is owed total 900
        val balances = listOf(
            balance(creditor, "3000", "2100"),  // net: +900
            balance(d1, "700", "1000"),          // net: -300
            balance(d2, "700", "1000"),          // net: -300
            balance(d3, "700", "1000")           // net: -300
        )
        val result = simplifier.simplify(balances, Currency.PKR)
        assertEquals(3, result.size)
        result.forEach { assertEquals("creditor", it.to.id) }
    }

    /** Test: currency is preserved */
    @Test
    fun `currency is preserved in transactions`() {
        val a = user("a")
        val b = user("b")
        val result = simplifier.simplify(
            listOf(balance(a, "50", "0"), balance(b, "0", "50")),
            Currency.USD
        )
        assertEquals(Currency.USD, result[0].currency)
    }
}

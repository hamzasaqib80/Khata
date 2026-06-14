package com.khata.app.test

import com.khata.app.domain.model.*
import com.khata.app.domain.usecase.CalculateMessBillUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class CalculateMessBillUseCaseTest {

    private lateinit var useCase: CalculateMessBillUseCase

    @Before
    fun setup() {
        useCase = CalculateMessBillUseCase()
    }

    private fun user(id: String) = User(id, id, "#1B6B5C", null, false)
    private fun plan(cost: String) = MealPlan(
        id = "plan1",
        groupId = "g1",
        month = 6,
        year = 2026,
        totalGroceryCost = BigDecimal(cost),
        breakfastWeight = BigDecimal("0.5"),
        lunchWeight = BigDecimal("1.0"),
        dinnerWeight = BigDecimal("1.0")
    )

    data class UserMealCounts(val userId: String, val breakfastCount: Int, val lunchCount: Int, val dinnerCount: Int)

    /** Test: equal meals → equal bills */
    @Test
    fun `equal meals - equal bills`() {
        val a = user("a")
        val b = user("b")
        val mealPlan = plan("1000")
        val counts = mapOf(
            a to com.khata.app.domain.model.UserMealBreakdown(10, 20, 20, BigDecimal.ZERO, BigDecimal.ZERO),
            b to com.khata.app.domain.model.UserMealBreakdown(10, 20, 20, BigDecimal.ZERO, BigDecimal.ZERO)
        )
        // Both have same weighted units: (10*0.5 + 20*1.0 + 20*1.0) = 5 + 20 + 20 = 45
        // Total = 90, costPerUnit = 1000/90 ≈ 11.11
        // Each = 45 * 11.11 = 500
        val result = useCase(mealPlan, mapOf(a to com.khata.app.data.local.dao.UserMealCounts("a", 10, 20, 20),
            b to com.khata.app.data.local.dao.UserMealCounts("b", 10, 20, 20)))
        val billA = result.perUserBill[a]!!
        val billB = result.perUserBill[b]!!
        assertEquals(billA.setScale(0, java.math.RoundingMode.HALF_UP),
            billB.setScale(0, java.math.RoundingMode.HALF_UP))
    }

    /** Test: zero grocery cost returns empty result */
    @Test
    fun `zero grocery cost - empty result`() {
        val a = user("a")
        val mealPlan = plan("0")
        val result = useCase(mealPlan, mapOf(a to com.khata.app.data.local.dao.UserMealCounts("a", 5, 10, 10)))
        // cost per unit should be 0
        assertTrue(result.perUserBill.values.all { it == BigDecimal.ZERO || it.compareTo(BigDecimal.ZERO) == 0 })
    }

    /** Test: bills sum to total grocery cost */
    @Test
    fun `all bills sum to total grocery cost`() {
        val a = user("a")
        val b = user("b")
        val c = user("c")
        val totalCost = BigDecimal("3000")
        val mealPlan = plan("3000")
        val result = useCase(mealPlan, mapOf(
            a to com.khata.app.data.local.dao.UserMealCounts("a", 5, 20, 20),
            b to com.khata.app.data.local.dao.UserMealCounts("b", 10, 25, 15),
            c to com.khata.app.data.local.dao.UserMealCounts("c", 0, 30, 25)
        ))
        val totalBills = result.perUserBill.values.fold(BigDecimal.ZERO) { acc, it -> acc + it }
        // Allow ±1 for rounding
        assertTrue(
            "Total bills ${totalBills} should approximate ${totalCost}",
            (totalBills - totalCost).abs() < BigDecimal("1.00")
        )
    }
}

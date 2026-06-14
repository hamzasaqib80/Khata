package com.khata.app.domain.usecase

import com.khata.app.domain.model.*
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateMessBillUseCase @Inject constructor() {
    
    operator fun invoke(
        mealPlan: MealPlan,
        userMealCounts: Map<User, UserMealCounts>
    ): MessBillResult {
        val breakdown = mutableMapOf<User, UserMealBreakdown>()
        var totalUnitsAllUsers = BigDecimal.ZERO

        userMealCounts.forEach { (user, counts) ->
            val weightedUnits = (BigDecimal(counts.breakfastCount) * mealPlan.breakfastWeight) +
                              (BigDecimal(counts.lunchCount) * mealPlan.lunchWeight) +
                              (BigDecimal(counts.dinnerCount) * mealPlan.dinnerWeight)
            
            totalUnitsAllUsers += weightedUnits
            breakdown[user] = UserMealBreakdown(
                breakfastCount = counts.breakfastCount,
                lunchCount = counts.lunchCount,
                dinnerCount = counts.dinnerCount,
                totalWeightedUnits = weightedUnits,
                billAmount = BigDecimal.ZERO // computed next
            )
        }

        if (totalUnitsAllUsers.compareTo(BigDecimal.ZERO) == 0) {
            return MessBillResult(emptyMap(), BigDecimal.ZERO, BigDecimal.ZERO, emptyMap())
        }

        val costPerUnit = mealPlan.totalGroceryCost.divide(totalUnitsAllUsers, 10, RoundingMode.HALF_UP)
        val perUserBill = mutableMapOf<User, BigDecimal>()
        val finalBreakdown = mutableMapOf<User, UserMealBreakdown>()

        breakdown.forEach { (user, b) ->
            val billAmount = (costPerUnit * b.totalWeightedUnits).setScale(2, RoundingMode.HALF_UP)
            perUserBill[user] = billAmount
            finalBreakdown[user] = b.copy(billAmount = billAmount)
        }

        return MessBillResult(
            perUserBill = perUserBill,
            totalUnits = totalUnitsAllUsers,
            costPerUnit = costPerUnit,
            breakdown = finalBreakdown
        )
    }
}

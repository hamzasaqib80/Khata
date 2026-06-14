package com.khata.app.data.repository

import com.khata.app.core.utils.Result
import com.khata.app.core.utils.asResult
import com.khata.app.core.utils.safeCall
import com.khata.app.data.local.dao.MealDao
import com.khata.app.data.local.entity.MealLogEntity
import com.khata.app.data.local.entity.MealPlanEntity
import com.khata.app.domain.model.*
import com.khata.app.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {

    override fun observeMealPlan(groupId: String, month: Int, year: Int): Flow<Result<MealPlan?>> {
        return mealDao.observeMealPlanForMonth(groupId, month, year)
            .map { it?.toDomain() }
            .asResult()
    }

    override fun observeMealLogs(mealPlanId: String): Flow<Result<List<MealLog>>> {
        return mealDao.observeMealLogsForPlan(mealPlanId)
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()
    }

    override suspend fun upsertMealLog(log: MealLog, mealPlanId: String): Result<Unit> = safeCall {
        mealDao.upsertMealLog(log.toEntity(mealPlanId))
    }

    override suspend fun saveMealPlan(plan: MealPlan): Result<Unit> = safeCall {
        mealDao.insertMealPlan(plan.toEntity())
    }

    override suspend fun getMealCounts(mealPlanId: String): Result<Map<String, UserMealBreakdown>> = safeCall {
        val counts = mealDao.getMealCountsPerUser(mealPlanId)
        counts.associate { 
            it.userId to UserMealBreakdown(
                breakfastCount = it.breakfastCount,
                lunchCount = it.lunchCount,
                dinnerCount = it.dinnerCount,
                totalWeightedUnits = BigDecimal.ZERO, // computed in use case
                billAmount = BigDecimal.ZERO // computed in use case
            )
        }
    }

    private fun MealPlanEntity.toDomain() = MealPlan(
        id = id,
        groupId = groupId,
        month = month,
        year = year,
        totalGroceryCost = BigDecimal(totalGroceryCost),
        breakfastWeight = BigDecimal(breakfastWeight),
        lunchWeight = BigDecimal(lunchWeight),
        dinnerWeight = BigDecimal(dinnerWeight)
    )

    private fun MealPlan.toEntity() = MealPlanEntity(
        id = id,
        groupId = groupId,
        month = month,
        year = year,
        totalGroceryCost = totalGroceryCost.toPlainString(),
        breakfastWeight = breakfastWeight.toPlainString(),
        lunchWeight = lunchWeight.toPlainString(),
        dinnerWeight = dinnerWeight.toPlainString(),
        createdAt = System.currentTimeMillis()
    )

    private fun MealLogEntity.toDomain() = MealLog(
        id = id,
        userId = userId,
        date = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate(),
        hadBreakfast = hadBreakfast,
        hadLunch = hadLunch,
        hadDinner = hadDinner
    )

    private fun MealLog.toEntity(mealPlanId: String) = MealLogEntity(
        id = id,
        mealPlanId = mealPlanId,
        userId = userId,
        date = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        hadBreakfast = hadBreakfast,
        hadLunch = hadLunch,
        hadDinner = hadDinner,
        notes = null
    )
}

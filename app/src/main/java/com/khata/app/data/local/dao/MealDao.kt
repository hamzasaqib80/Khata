package com.khata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khata.app.data.local.entity.MealLogEntity
import com.khata.app.data.local.entity.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_plans WHERE groupId = :groupId AND month = :month AND year = :year LIMIT 1")
    fun observeMealPlanForMonth(groupId: String, month: Int, year: Int): Flow<MealPlanEntity?>

    @Query("SELECT * FROM meal_logs WHERE mealPlanId = :mealPlanId ORDER BY date ASC")
    fun observeMealLogsForPlan(mealPlanId: String): Flow<List<MealLogEntity>>

    @Query("SELECT * FROM meal_logs WHERE mealPlanId = :mealPlanId AND userId = :userId ORDER BY date ASC")
    fun observeMealLogsForUserAndPlan(mealPlanId: String, userId: String): Flow<List<MealLogEntity>>

    @Query("""
        SELECT userId,
               SUM(CASE WHEN hadBreakfast = 1 THEN 1 ELSE 0 END) as breakfastCount,
               SUM(CASE WHEN hadLunch = 1 THEN 1 ELSE 0 END) as lunchCount,
               SUM(CASE WHEN hadDinner = 1 THEN 1 ELSE 0 END) as dinnerCount
        FROM meal_logs
        WHERE mealPlanId = :mealPlanId
        GROUP BY userId
    """)
    suspend fun getMealCountsPerUser(mealPlanId: String): List<UserMealCounts>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(plan: MealPlanEntity): Long

    @Update
    suspend fun updateMealPlan(plan: MealPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMealLog(log: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :logId")
    suspend fun deleteMealLog(logId: String)

    @Query("SELECT * FROM meal_plans WHERE id = :mealPlanId LIMIT 1")
    suspend fun getMealPlanById(mealPlanId: String): MealPlanEntity?
}

data class UserMealCounts(
    val userId: String,
    val breakfastCount: Int,
    val lunchCount: Int,
    val dinnerCount: Int
)

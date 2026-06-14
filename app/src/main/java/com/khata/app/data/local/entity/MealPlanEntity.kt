package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_plans",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"]
        )
    ],
    indices = [
        Index("groupId"),
        Index("groupId", "year", "month", unique = true)
    ]
)
data class MealPlanEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val month: Int, // 1-12
    val year: Int,
    val totalGroceryCost: String, // BigDecimal as String
    val breakfastWeight: String = "0.5",
    val lunchWeight: String = "1.0",
    val dinnerWeight: String = "1.0",
    val isClosed: Boolean = false,
    val createdAt: Long
)

package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meal_logs",
    foreignKeys = [
        ForeignKey(
            entity = MealPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = [
        Index("mealPlanId"),
        Index("userId"),
        Index("date"),
        Index("mealPlanId", "userId", "date", unique = true)
    ]
)
data class MealLogEntity(
    @PrimaryKey val id: String,
    val mealPlanId: String,
    val userId: String,
    val date: Long, // epoch ms
    val hadBreakfast: Boolean,
    val hadLunch: Boolean,
    val hadDinner: Boolean,
    val notes: String?
)

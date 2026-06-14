package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_participants",
    foreignKeys = [
        ForeignKey(
            entity = ExpenseEntity::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = [
        Index("expenseId"),
        Index("userId"),
        Index("expenseId", "userId", unique = true)
    ]
)
data class ExpenseParticipantEntity(
    @PrimaryKey val id: String,
    val expenseId: String,
    val userId: String,
    val shareAmount: String, // BigDecimal as String
    val sharePercentage: String?, // BigDecimal as String
    val isPaid: Boolean = false
)

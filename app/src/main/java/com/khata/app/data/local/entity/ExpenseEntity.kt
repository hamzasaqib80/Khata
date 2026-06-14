package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["payerId"]
        )
    ],
    indices = [
        Index("groupId"),
        Index("payerId"),
        Index("date"),
        Index("description"),
        Index("groupId", "date")
    ]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val payerId: String,
    val description: String,
    val amount: String, // BigDecimal as String
    val currency: String,
    val splitType: String, // EQUAL/UNEQUAL/PERCENTAGE
    val category: String, // FOOD/UTILITIES/RENT/TRANSPORT/GROCERIES/OTHER
    val receiptNote: String?,
    val date: Long, // epoch ms
    val createdAt: Long
)

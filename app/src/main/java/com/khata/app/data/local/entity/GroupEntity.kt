package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index("name"),
        Index("currency")
    ]
)
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val currency: String, // enum name: PKR/USD/EUR/GBP/SAR/AED
    val memberIds: String, // JSON list
    val totalExpenses: String, // BigDecimal as String
    val createdAt: Long,
    val updatedAt: Long
)

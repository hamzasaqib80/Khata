package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlements",
    foreignKeys = [
        ForeignKey(
            entity = GroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["groupId"]
        )
    ],
    indices = [
        Index("groupId"),
        Index("payerUserId"),
        Index("receiverUserId"),
        Index("settledAt")
    ]
)
data class SettlementEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val payerUserId: String,
    val receiverUserId: String,
    val amount: String, // BigDecimal as String
    val currency: String,
    val paymentMethod: String, // CASH/EASYPAISA/JAZZCASH/BANK_TRANSFER
    val transactionReference: String?,
    val settledAt: Long,
    val note: String?
)

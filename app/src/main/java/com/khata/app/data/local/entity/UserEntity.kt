package com.khata.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index("name"),
        Index("isCurrentUser")
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarColorHex: String,
    val phoneNumber: String?,
    val roomNo: String?,
    val isCurrentUser: Boolean,
    val groupIds: String, // JSON-serialized list via TypeConverter
    val createdAt: Long
)

package com.khata.app.data.local.mapper

import com.khata.app.data.local.entity.UserEntity
import com.khata.app.domain.model.User

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    avatarColorHex = avatarColorHex,
    phoneNumber = phoneNumber,
    roomNo = roomNo,
    isCurrentUser = isCurrentUser
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    avatarColorHex = avatarColorHex,
    phoneNumber = phoneNumber,
    roomNo = roomNo,
    isCurrentUser = isCurrentUser,
    groupIds = "", // handle if needed
    createdAt = System.currentTimeMillis()
)

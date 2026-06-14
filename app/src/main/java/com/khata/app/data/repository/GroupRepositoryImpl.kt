package com.khata.app.data.repository

import com.khata.app.core.utils.Result
import com.khata.app.core.utils.asResult
import com.khata.app.core.utils.safeCall
import com.khata.app.data.local.dao.GroupDao
import com.khata.app.data.local.dao.UserDao
import com.khata.app.data.local.entity.GroupEntity
import com.khata.app.data.local.mapper.toDomain
import com.khata.app.data.local.mapper.toEntity
import com.khata.app.domain.model.Currency
import com.khata.app.domain.model.Group
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.math.BigDecimal
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val userDao: UserDao
) : GroupRepository {

    override fun observeAllGroups(): Flow<Result<List<Group>>> {
        return groupDao.observeAllGroups().map { entities ->
            entities.map { it.toDomain(emptyList()) }
        }.asResult()
    }

    override fun observeGroupById(groupId: String): Flow<Result<Group?>> {
        return groupDao.observeGroupById(groupId).flatMapLatest { entity ->
            if (entity == null) return@flatMapLatest kotlinx.coroutines.flow.flowOf(null)
            
            val memberIds = entity.memberIds.split(",").filter { it.isNotBlank() }
            userDao.observeGroupMembers(memberIds).map { memberEntities ->
                entity.toDomain(memberEntities.map { it.toDomain() })
            }
        }.asResult()
    }

    override suspend fun createGroup(group: Group): Result<Unit> = safeCall {
        groupDao.insertGroup(group.toEntity())
    }

    override suspend fun getGroupById(groupId: String): Result<Group?> = safeCall {
        val entity = groupDao.getGroupById(groupId) ?: return@safeCall null
        val memberIds = entity.memberIds.split(",").filter { it.isNotBlank() }
        val memberEntities = userDao.getUsersByIds(memberIds)
        entity.toDomain(memberEntities.map { it.toDomain() })
    }

    override suspend fun updateGroup(group: Group): Result<Unit> = safeCall {
        groupDao.updateGroup(group.toEntity())
    }

    override suspend fun addMemberToGroup(groupId: String, user: User): Result<Unit> = safeCall {
        // 1. Insert/Update the user
        userDao.insertUser(user.toEntity())
        
        // 2. Fetch current group
        val groupEntity = groupDao.getGroupById(groupId) ?: throw Exception("Group not found")
        
        // 3. Append user ID to memberIds
        val currentIds = groupEntity.memberIds.split(",").filter { it.isNotBlank() }.toMutableList()
        if (!currentIds.contains(user.id)) {
            currentIds.add(user.id)
            val updatedEntity = groupEntity.copy(
                memberIds = currentIds.joinToString(","),
                updatedAt = System.currentTimeMillis()
            )
            groupDao.updateGroup(updatedEntity)
        }
    }

    private fun GroupEntity.toDomain(members: List<User>) = Group(
        id = id,
        name = name,
        description = description,
        currency = Currency.valueOf(currency),
        members = members,
        totalExpenses = BigDecimal(totalExpenses)
    )

    private fun Group.toEntity() = GroupEntity(
        id = id,
        name = name,
        description = description,
        currency = currency.name,
        memberIds = members.joinToString(",") { it.id },
        totalExpenses = totalExpenses.toPlainString(),
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}

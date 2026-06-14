package com.khata.app.data.repository

import com.khata.app.core.utils.Result
import com.khata.app.core.utils.asResult
import com.khata.app.core.utils.safeCall
import com.khata.app.data.local.dao.UserDao
import com.khata.app.data.local.mapper.toDomain
import com.khata.app.data.local.mapper.toEntity
import com.khata.app.domain.model.User
import com.khata.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : UserRepository {

    override fun observeAllUsers(): Flow<Result<List<User>>> {
        return userDao.observeAllUsers()
            .map { entities -> entities.map { it.toDomain() } }
            .asResult()
    }

    override fun observeCurrentUser(): Flow<Result<User?>> {
        return userDao.observeCurrentUser()
            .map { it?.toDomain() }
            .asResult()
    }

    override suspend fun saveUser(user: User): Result<Unit> = safeCall {
        userDao.insertUser(user.toEntity())
    }

    override suspend fun getUserById(userId: String): Result<User?> = safeCall {
        userDao.getUserById(userId)?.toDomain()
    }
}

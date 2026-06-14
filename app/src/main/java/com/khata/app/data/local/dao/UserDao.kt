package com.khata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.khata.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id IN (:memberIds)")
    fun observeGroupMembers(memberIds: List<String>): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id IN (:ids)")
    suspend fun getUsersByIds(ids: List<String>): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSnapshot(): List<UserEntity>
}

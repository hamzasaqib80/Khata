package com.khata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.khata.app.data.local.entity.SettlementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettlementDao {
    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY settledAt DESC")
    fun observeSettlementsForGroup(groupId: String): Flow<List<SettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity)

    @Query("SELECT * FROM settlements WHERE groupId = :groupId ORDER BY settledAt DESC")
    suspend fun getSettlementsByGroup(groupId: String): List<SettlementEntity>

    @Query("""
        SELECT payerUserId as userId, SUM(CAST(amount AS REAL)) as totalAmount
        FROM settlements
        WHERE groupId = :groupId
        GROUP BY payerUserId
    """)
    suspend fun getTotalPaidSettlementsPerUser(groupId: String): List<UserSettlementAmount>

    @Query("""
        SELECT payerUserId as userId, SUM(CAST(amount AS REAL)) as totalAmount
        FROM settlements
        WHERE groupId = :groupId
        GROUP BY payerUserId
    """)
    fun observeTotalPaidSettlementsPerUser(groupId: String): Flow<List<UserSettlementAmount>>

    @Query("""
        SELECT receiverUserId as userId, SUM(CAST(amount AS REAL)) as totalAmount
        FROM settlements
        WHERE groupId = :groupId
        GROUP BY receiverUserId
    """)
    suspend fun getTotalReceivedSettlementsPerUser(groupId: String): List<UserSettlementAmount>

    @Query("""
        SELECT receiverUserId as userId, SUM(CAST(amount AS REAL)) as totalAmount
        FROM settlements
        WHERE groupId = :groupId
        GROUP BY receiverUserId
    """)
    fun observeTotalReceivedSettlementsPerUser(groupId: String): Flow<List<UserSettlementAmount>>
}

data class UserSettlementAmount(
    val userId: String,
    val totalAmount: Double
)

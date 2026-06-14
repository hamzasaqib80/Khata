package com.khata.app.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.khata.app.core.security.DatabaseKeyProvider
import com.khata.app.data.local.converter.BigDecimalConverter
import com.khata.app.data.local.converter.DateConverter
import com.khata.app.data.local.converter.StringListConverter
import com.khata.app.data.local.dao.*
import com.khata.app.data.local.entity.*
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        UserEntity::class,
        GroupEntity::class,
        ExpenseEntity::class,
        ExpenseParticipantEntity::class,
        MealPlanEntity::class,
        MealLogEntity::class,
        SettlementEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(
    DateConverter::class,
    BigDecimalConverter::class,
    StringListConverter::class
)
abstract class KhataDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun mealDao(): MealDao
    abstract fun settlementDao(): SettlementDao

    companion object {
        @Volatile
        private var INSTANCE: KhataDatabase? = null

        fun getDatabase(context: Context, keyProvider: DatabaseKeyProvider): KhataDatabase {
            return INSTANCE ?: synchronized(this) {
                val passphrase = SQLiteDatabase.getBytes(keyProvider.getDatabaseKey().toCharArray())
                val factory = SupportFactory(passphrase)

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KhataDatabase::class.java,
                    "khata_database"
                )
                .openHelperFactory(factory)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
}

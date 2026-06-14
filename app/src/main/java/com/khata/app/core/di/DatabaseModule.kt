package com.khata.app.core.di

import android.content.Context
import com.khata.app.core.security.DatabaseKeyProvider
import com.khata.app.data.local.KhataDatabase
import com.khata.app.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKhataDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider
    ): KhataDatabase {
        return KhataDatabase.getDatabase(context, keyProvider)
    }

    @Provides
    fun provideUserDao(database: KhataDatabase): UserDao = database.userDao()

    @Provides
    fun provideGroupDao(database: KhataDatabase): GroupDao = database.groupDao()

    @Provides
    fun provideExpenseDao(database: KhataDatabase): ExpenseDao = database.expenseDao()

    @Provides
    fun provideMealDao(database: KhataDatabase): MealDao = database.mealDao()

    @Provides
    fun provideSettlementDao(database: KhataDatabase): SettlementDao = database.settlementDao()
}

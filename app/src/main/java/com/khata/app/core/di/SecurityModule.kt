package com.khata.app.core.di

import android.content.Context
import com.khata.app.core.security.BiometricAuthManager
import com.khata.app.core.security.DatabaseKeyProvider
import com.khata.app.core.security.SecurePreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideSecurePreferences(@ApplicationContext context: Context): SecurePreferences {
        return SecurePreferences(context)
    }

    @Provides
    @Singleton
    fun provideDatabaseKeyProvider(
        @ApplicationContext context: Context,
        securePreferences: SecurePreferences
    ): DatabaseKeyProvider {
        return DatabaseKeyProvider(context, securePreferences)
    }

    @Provides
    @Singleton
    fun provideBiometricAuthManager(@ApplicationContext context: Context): BiometricAuthManager {
        return BiometricAuthManager(context)
    }
}

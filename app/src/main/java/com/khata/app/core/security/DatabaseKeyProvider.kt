package com.khata.app.core.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides a cryptographically secure key for SQLCipher database encryption.
 * The key is generated once and stored in EncryptedSharedPreferences.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferences: SecurePreferences
) {
    private companion object {
        const val PREFS_KEY_DB_PASSPHRASE = "db_passphrase_encrypted"
    }

    /**
     * Retrieves the database key, generating it if it doesn't already exist.
     */
    fun getDatabaseKey(): String {
        val existingKey = securePreferences.getString(PREFS_KEY_DB_PASSPHRASE)
        return if (existingKey != null) {
            existingKey
        } else {
            val newKey = generateSecurePassphrase()
            securePreferences.putString(PREFS_KEY_DB_PASSPHRASE, newKey)
            newKey
        }
    }

    private fun generateSecurePassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}

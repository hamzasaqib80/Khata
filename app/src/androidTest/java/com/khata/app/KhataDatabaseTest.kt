package com.khata.app.test

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.khata.app.data.local.KhataDatabase
import com.khata.app.data.local.dao.UserDao
import com.khata.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented Room database tests for Khata.
 * Uses an in-memory database to avoid affecting production data.
 */
@RunWith(AndroidJUnit4::class)
class KhataDatabaseTest {

    private lateinit var database: KhataDatabase
    private lateinit var userDao: UserDao

    @Before
    fun createDatabase() {
        // Note: In-memory Room database does NOT use SQLCipher encryption
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            KhataDatabase::class.java
        ).allowMainThreadQueries().build()
        userDao = database.userDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndRetrieveUser() = runTest {
        val user = UserEntity(
            id = "user1",
            name = "Ali Hassan",
            avatarColorHex = "#1B6B5C",
            phoneNumber = "03001234567",
            isCurrentUser = true,
            groupIds = "",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
        val retrieved = userDao.getUserById("user1")
        assertNotNull(retrieved)
        assertEquals("Ali Hassan", retrieved!!.name)
        assertTrue(retrieved.isCurrentUser)
    }

    @Test
    fun observeCurrentUser() = runTest {
        val user = UserEntity(
            id = "current",
            name = "Current User",
            avatarColorHex = "#1B6B5C",
            phoneNumber = null,
            isCurrentUser = true,
            groupIds = "",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
        val currentUser = userDao.observeCurrentUser().first()
        assertNotNull(currentUser)
        assertEquals("current", currentUser!!.id)
    }

    @Test
    fun deleteUser() = runTest {
        val user = UserEntity(
            id = "toDelete",
            name = "To Delete",
            avatarColorHex = "#FF0000",
            phoneNumber = null,
            isCurrentUser = false,
            groupIds = "",
            createdAt = System.currentTimeMillis()
        )
        userDao.insertUser(user)
        userDao.deleteUser("toDelete")
        val retrieved = userDao.getUserById("toDelete")
        assertNull(retrieved)
    }
}

package com.example.pocketmanage.auth

import android.content.Context
import android.util.Base64
import com.example.pocketmanage.data.AppDatabase
import com.example.pocketmanage.data.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalAuthRepository {
    suspend fun register(
        context: Context,
        email: String,
        password: String,
        displayName: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val db = AppDatabase.get(context)
        val normalized = email.trim().lowercase()
        if (db.userDao().getByEmail(normalized) != null) {
            return@withContext Result.failure(IllegalStateException("email_taken"))
        }
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        val user = UserEntity(
            email = normalized,
            displayName = displayName.trim(),
            passwordHashB64 = Base64.encodeToString(hash, Base64.NO_WRAP),
            saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP),
        )
        val id = db.userDao().insert(user)
        LocalAuth.signIn(id)
        Result.success(Unit)
    }

    suspend fun login(context: Context, email: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = AppDatabase.get(context)
            val normalized = email.trim().lowercase()
            val user = db.userDao().getByEmail(normalized)
                ?: return@withContext Result.failure(IllegalStateException("bad_credentials"))
            if (!PasswordHasher.verify(password, user.saltB64, user.passwordHashB64)) {
                return@withContext Result.failure(IllegalStateException("bad_credentials"))
            }
            LocalAuth.signIn(user.id)
            Result.success(Unit)
        }

    suspend fun updateDisplayName(context: Context, userId: Long, displayName: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val db = AppDatabase.get(context)
            val user = db.userDao().getById(userId) ?: return@withContext Result.failure(IllegalStateException("no_user"))
            db.userDao().update(user.copy(displayName = displayName.trim()))
            Result.success(Unit)
        }

    suspend fun changePassword(
        context: Context,
        userId: Long,
        currentPassword: String,
        newPassword: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (newPassword.length < 4) {
            return@withContext Result.failure(IllegalStateException("password_short"))
        }
        val db = AppDatabase.get(context)
        val user = db.userDao().getById(userId) ?: return@withContext Result.failure(IllegalStateException("no_user"))
        if (!PasswordHasher.verify(currentPassword, user.saltB64, user.passwordHashB64)) {
            return@withContext Result.failure(IllegalStateException("wrong_password"))
        }
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(newPassword, salt)
        db.userDao().update(
            user.copy(
                passwordHashB64 = Base64.encodeToString(hash, Base64.NO_WRAP),
                saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP),
            ),
        )
        Result.success(Unit)
    }
}

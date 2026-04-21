package com.example.pocketmanage.auth

import android.app.Application
import android.content.Context

object LocalAuth {
    private lateinit var app: Application

    fun init(application: Application) {
        app = application
    }

    private val prefs
        get() = app.getSharedPreferences("local_auth", Context.MODE_PRIVATE)

    private const val KEY_USER_ID = "user_id"

    fun currentUserId(): Long? {
        if (!::app.isInitialized) return null
        val id = prefs.getLong(KEY_USER_ID, -1L)
        return if (id >= 0) id else null
    }

    fun isSignedIn(): Boolean = currentUserId() != null

    fun signIn(userId: Long) {
        prefs.edit().putLong(KEY_USER_ID, userId).apply()
    }

    fun signOut() {
        prefs.edit().remove(KEY_USER_ID).apply()
    }
}

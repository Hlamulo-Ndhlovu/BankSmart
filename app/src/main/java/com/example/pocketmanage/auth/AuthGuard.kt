package com.example.pocketmanage.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketmanage.LoginActivity
import com.example.pocketmanage.MainActivity

object AuthGuard {
    fun requireSignedIn(activity: AppCompatActivity) {
        if (!LocalAuth.isSignedIn()) {
            activity.startActivity(Intent(activity, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            activity.finish()
        }
    }

    /** Call from Login/Register: skip auth screens when already signed in. */
    fun openMainIfSignedIn(activity: AppCompatActivity) {
        if (LocalAuth.isSignedIn()) {
            activity.startActivity(Intent(activity, MainActivity::class.java))
            activity.finish()
        }
    }
}

package com.example.pocketmanage

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.pocketmanage.auth.AuthGuard
import com.example.pocketmanage.auth.LocalAuthRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import android.widget.TextView
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val mainView = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.createAccountText).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.loginButton).setOnClickListener {
            val email = findViewById<TextInputEditText>(R.id.emailEditText).text?.toString()?.trim().orEmpty()
            val password = findViewById<TextInputEditText>(R.id.passwordEditText).text?.toString().orEmpty()
            if (email.isEmpty() || password.isEmpty()) {
                Snackbar.make(mainView, R.string.validation_email_password, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val result = LocalAuthRepository.login(this@LoginActivity, email, password)
                if (result.isSuccess) {
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    val msg = when (result.exceptionOrNull()?.message) {
                        "bad_credentials" -> getString(R.string.auth_wrong_credentials)
                        else -> getString(R.string.auth_failed)
                    }
                    Snackbar.make(mainView, msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }

        findViewById<TextView>(R.id.forgotPasswordText).setOnClickListener {
            Snackbar.make(mainView, R.string.auth_local_password_reset, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.openMainIfSignedIn(this)
    }
}

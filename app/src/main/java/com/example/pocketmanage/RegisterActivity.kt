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

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        val mainView = findViewById<androidx.coordinatorlayout.widget.CoordinatorLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.signInText).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.registerButton).setOnClickListener {
            val fullName = findViewById<TextInputEditText>(R.id.fullNameEditText).text?.toString()?.trim().orEmpty()
            val email = findViewById<TextInputEditText>(R.id.emailEditText).text?.toString()?.trim().orEmpty()
            val password = findViewById<TextInputEditText>(R.id.passwordEditText).text?.toString().orEmpty()
            val confirm = findViewById<TextInputEditText>(R.id.confirmPasswordEditText).text?.toString().orEmpty()
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Snackbar.make(mainView, R.string.validation_register, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirm) {
                Snackbar.make(mainView, R.string.validation_register, Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val result = LocalAuthRepository.register(this@RegisterActivity, email, password, fullName)
                if (result.isSuccess) {
                    goToMain()
                } else {
                    val msg = when (result.exceptionOrNull()?.message) {
                        "email_taken" -> getString(R.string.auth_email_taken)
                        else -> getString(R.string.auth_register_failed)
                    }
                    Snackbar.make(mainView, msg, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        AuthGuard.openMainIfSignedIn(this)
    }
}

package com.example.wellnessbuddy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Set status bar icons to dark (black) for better visibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.decorView.systemUiVisibility =
                window.decorView.systemUiVisibility or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Validation
            when {
                name.isEmpty() -> etName.error = "Enter your name"
                email.isEmpty() -> etEmail.error = "Enter your email"
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> etEmail.error = "Invalid email"
                password.isEmpty() -> etPassword.error = "Enter your password"
                password.length < 6 -> etPassword.error = "Password must be at least 6 characters"
                confirmPassword.isEmpty() -> etConfirmPassword.error = "Confirm your password"
                password != confirmPassword -> etConfirmPassword.error = "Passwords do not match"
                else -> {
                    // Save to SharedPreferences
                    sharedPreferences.edit().apply {
                        putString("user_name", name)
                        putString("user_email", email)
                        putString("user_password", password)
                        apply()
                    }
                    Toast.makeText(this, "Signup successful!", Toast.LENGTH_SHORT).show()

                    // Go to Login
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}

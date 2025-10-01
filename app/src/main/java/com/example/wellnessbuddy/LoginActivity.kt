package com.example.wellnessbuddy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignup = findViewById<TextView>(R.id.tvSignup)

        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            val savedEmail = sharedPreferences.getString("user_email", null)
            val savedPassword = sharedPreferences.getString("user_password", null)

            when {
                email.isEmpty() -> etEmail.error = "Enter your email"
                password.isEmpty() -> etPassword.error = "Enter your password"
                savedEmail == null || savedPassword == null -> {
                    Toast.makeText(this, "No user found. Please signup first.", Toast.LENGTH_SHORT).show()
                }
                email != savedEmail || password != savedPassword -> {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}

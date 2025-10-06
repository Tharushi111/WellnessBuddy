package com.example.wellnessbuddy

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wellnessbuddy.util.SharedPreferencesHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlin.math.pow

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etWeight: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var tvBMI: TextView
    private lateinit var tvBMIStatus: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var toolbar: MaterialToolbar

    private lateinit var sharedHelper: SharedPreferencesHelper
    private lateinit var loggedUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // --- Toolbar Setup with White Title & Close Icon ---
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        // Set toolbar title and navigation icon colors to white
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        // Close activity on navigation click
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // --- Initialize Views ---
        etName = findViewById(R.id.etName)
        etEmail = findViewById(R.id.etEmail)
        etAge = findViewById(R.id.etAge)
        etWeight = findViewById(R.id.etWeight)
        etHeight = findViewById(R.id.etHeight)
        tvBMI = findViewById(R.id.tvBMI)
        tvBMIStatus = findViewById(R.id.tvBMIStatus)
        btnSave = findViewById(R.id.btnSave)

        // Get logged-in user email
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        loggedUserEmail = prefs.getString("logged_user_email", "") ?: ""
        if (loggedUserEmail.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initialize SharedPreferencesHelper
        sharedHelper = SharedPreferencesHelper(this, loggedUserEmail)

        // Load saved user data
        loadUserData()

        // Setup BMI auto-calculation
        setupBMICalculation()

        // Save button click
        btnSave.setOnClickListener {
            saveUserData()
        }
    }

    private fun setupBMICalculation() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateBMI()
            }
        }

        etWeight.addTextChangedListener(textWatcher)
        etHeight.addTextChangedListener(textWatcher)
    }

    private fun calculateBMI() {
        val weightStr = etWeight.text.toString()
        val heightStr = etHeight.text.toString()

        if (weightStr.isNotEmpty() && heightStr.isNotEmpty()) {
            try {
                val weight = weightStr.toDouble()
                val heightCm = heightStr.toDouble()
                val heightM = heightCm / 100

                if (weight > 0 && heightM > 0) {
                    val bmi = weight / (heightM.pow(2))
                    tvBMI.text = String.format("%.1f", bmi)

                    val (status, color) = when {
                        bmi < 18.5 -> Pair("Underweight", R.color.info)
                        bmi < 25.0 -> Pair("Normal", R.color.success)
                        bmi < 30.0 -> Pair("Overweight", R.color.warning)
                        else -> Pair("Obese", R.color.error)
                    }

                    tvBMIStatus.text = status
                    tvBMI.setTextColor(ContextCompat.getColor(this, color))
                } else {
                    resetBMI()
                }
            } catch (e: NumberFormatException) {
                resetBMI()
            }
        } else {
            resetBMI()
        }
    }

    private fun resetBMI() {
        tvBMI.text = "--"
        tvBMI.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
        tvBMIStatus.text = "Enter weight and height"
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        etName.setText(prefs.getString("logged_user_name", ""))
        etEmail.setText(loggedUserEmail)
        etAge.setText(prefs.getString("${loggedUserEmail}_age", ""))
        etWeight.setText(prefs.getString("${loggedUserEmail}_weight", ""))
        etHeight.setText(prefs.getString("${loggedUserEmail}_height", ""))
        calculateBMI()
    }

    private fun saveUserData() {
        val name = etName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val age = etAge.text.toString().trim()
        val weight = etWeight.text.toString().trim()
        val height = etHeight.text.toString().trim()

        if (name.isEmpty()) {
            etName.error = "Name is required"
            etName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            etEmail.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter valid email"
            etEmail.requestFocus()
            return
        }

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("logged_user_name", name)
            putString("logged_user_email", email)
            putString("${email}_age", age)
            putString("${email}_weight", weight)
            putString("${email}_height", height)
            apply()
        }

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

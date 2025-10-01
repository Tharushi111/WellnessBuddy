package com.example.wellnessbuddy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class LaunchScreenActivity : AppCompatActivity() {

    // Splash screen duration in milliseconds (3 seconds)
    private val splashTimeOut: Long = 3000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch_screen)

        // Hide action bar for full screen experience
        supportActionBar?.hide()

        // Check if onboarding is completed from SharedPreferences
        val sharedPreferences = getSharedPreferences(
            getString(R.string.pref_name),
            MODE_PRIVATE
        )
        val isOnboardingComplete = sharedPreferences.getBoolean(
            getString(R.string.pref_onboarding_complete),
            false
        )

        // Delayed navigation after splash timeout
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isOnboardingComplete) {
                // Navigate to Main Activity if onboarding is complete
                Intent(this, OnboardingActivity::class.java)
            } else {
                // Navigate to Onboarding if first time user
                Intent(this, OnboardingActivity::class.java)
            }
            startActivity(intent)
            finish() // Close launch screen so user can't go back to it
        }, splashTimeOut)
    }
}
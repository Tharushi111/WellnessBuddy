package com.example.wellnessbuddy

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var ivProfile: ImageView
    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        bottomNavigation = findViewById(R.id.bottomNavigation)
        ivProfile = findViewById(R.id.ivProfile)
        toolbar = findViewById(R.id.toolbar)

        // Properly handle status bar insets using WindowInsetsCompat
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { view, insets: WindowInsetsCompat ->
            val statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updatePadding(top = statusBarHeight)
            insets // return WindowInsetsCompat
        }

        // Load default fragment (Habits)
        if (savedInstanceState == null) {
            loadFragment(HabitsFragment())
        }

        // Bottom Navigation Item Selected Listener
        bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_habits -> {
                    loadFragment(HabitsFragment())
                    true
                }
                R.id.nav_mood -> {
                    loadFragment(MoodJournalFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Profile icon click listener
        ivProfile.setOnClickListener {
            android.widget.Toast.makeText(this, "Profile clicked", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Back press handling using OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this) {
            if (bottomNavigation.selectedItemId != R.id.nav_habits) {
                bottomNavigation.selectedItemId = R.id.nav_habits
            } else {
                showExitConfirmation()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showExitConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit Wellness Buddy?")
            .setPositiveButton("Exit") { _, _ -> finish() }
            .setNegativeButton("Cancel", null)
            .show()
    }
}

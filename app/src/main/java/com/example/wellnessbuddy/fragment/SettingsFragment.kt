package com.example.wellnessbuddy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var switchHydration: SwitchMaterial
    private lateinit var layoutInterval: LinearLayout
    private lateinit var layoutAbout: LinearLayout
    private lateinit var layoutLogout: LinearLayout
    private lateinit var tvIntervalValue: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        switchHydration = view.findViewById(R.id.switchHydration)
        layoutInterval = view.findViewById(R.id.layoutInterval)
        layoutAbout = view.findViewById(R.id.layoutAbout)
        layoutLogout = view.findViewById(R.id.layoutLogout)
        tvIntervalValue = view.findViewById(R.id.tvIntervalValue)

        // Load saved preferences
        loadPreferences()

        // Setup listeners
        setupListeners()

        return view
    }

    private fun loadPreferences() {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.pref_name),
            android.content.Context.MODE_PRIVATE
        )

        // Load hydration reminder status
        val isHydrationEnabled = sharedPreferences.getBoolean("hydration_enabled", false)
        switchHydration.isChecked = isHydrationEnabled

        // Load reminder interval
        val interval = sharedPreferences.getInt("reminder_interval", 60)
        tvIntervalValue.text = "$interval minutes"
    }

    private fun setupListeners() {
        // Hydration switch listener
        switchHydration.setOnCheckedChangeListener { _, isChecked ->
            saveHydrationPreference(isChecked)

            if (isChecked) {
                // TODO: Start hydration reminder service
                android.widget.Toast.makeText(
                    requireContext(),
                    "Hydration reminders enabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                // TODO: Stop hydration reminder service
                android.widget.Toast.makeText(
                    requireContext(),
                    "Hydration reminders disabled",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Interval click listener
        layoutInterval.setOnClickListener {
            showIntervalDialog()
        }

        // About click listener
        layoutAbout.setOnClickListener {
            showAboutDialog()
        }

        // Logout click listener
        layoutLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun saveHydrationPreference(isEnabled: Boolean) {
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.pref_name),
            android.content.Context.MODE_PRIVATE
        )
        sharedPreferences.edit()
            .putBoolean("hydration_enabled", isEnabled)
            .apply()
    }

    private fun showIntervalDialog() {
        val intervals = arrayOf("15 minutes", "30 minutes", "45 minutes", "60 minutes", "90 minutes", "120 minutes")
        val intervalValues = arrayOf(15, 30, 45, 60, 90, 120)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Reminder Interval")
            .setItems(intervals) { _, which ->
                val selectedInterval = intervalValues[which]
                tvIntervalValue.text = "$selectedInterval minutes"

                // Save to SharedPreferences
                val sharedPreferences = requireActivity().getSharedPreferences(
                    getString(R.string.pref_name),
                    android.content.Context.MODE_PRIVATE
                )
                sharedPreferences.edit()
                    .putInt("reminder_interval", selectedInterval)
                    .apply()

                android.widget.Toast.makeText(
                    requireContext(),
                    "Interval updated to $selectedInterval minutes",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
            .show()
    }

    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("About Wellness Buddy")
            .setMessage("Wellness Buddy v1.0\n\nYour daily health companion to track habits, log moods, and stay hydrated.\n\nDeveloped as part of IT2010 Mobile Application Development.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear login status
        val sharedPreferences = requireActivity().getSharedPreferences(
            getString(R.string.pref_name),
            android.content.Context.MODE_PRIVATE
        )
        sharedPreferences.edit()
            .putBoolean("is_logged_in", false)
            .apply()

        // Navigate to login screen
        val intent = Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}
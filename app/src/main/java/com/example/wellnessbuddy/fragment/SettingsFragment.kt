package com.example.wellnessbuddy

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.example.wellnessbuddy.util.SharedPreferencesHelper

class SettingsFragment : Fragment() {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var switchHydration: SwitchMaterial
    private lateinit var layoutInterval: LinearLayout
    private lateinit var layoutAbout: LinearLayout
    private lateinit var layoutLogout: LinearLayout
    private lateinit var tvIntervalValue: TextView

    private lateinit var prefsHelper: SharedPreferencesHelper
    private lateinit var hydrationManager: HydrationReminderManager

    // Permission launcher for Android 13+ notifications
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            enableHydrationReminder()
        } else {
            switchHydration.isChecked = false
            Toast.makeText(
                requireContext(),
                "Notification permission required for reminders",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Initialize views
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        switchHydration = view.findViewById(R.id.switchHydration)
        layoutInterval = view.findViewById(R.id.layoutInterval)
        layoutAbout = view.findViewById(R.id.layoutAbout)
        layoutLogout = view.findViewById(R.id.layoutLogout)
        tvIntervalValue = view.findViewById(R.id.tvIntervalValue)

        // Get logged-in user email
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val loggedUserEmail = sharedPreferences.getString("logged_user_email", "") ?: ""

        // Initialize helpers with user email
        prefsHelper = SharedPreferencesHelper(requireContext(), loggedUserEmail)
        hydrationManager = HydrationReminderManager(requireContext())

        // Load preferences & user info
        loadPreferences()
        loadUserInfo()
        setupListeners()

        return view
    }


    private fun loadPreferences() {
        // Load hydration reminder status
        val isHydrationEnabled = prefsHelper.isHydrationEnabled()
        switchHydration.isChecked = isHydrationEnabled

        // Load reminder interval
        val interval = prefsHelper.getReminderInterval()
        tvIntervalValue.text = "$interval minutes"
    }

    private fun loadUserInfo() {
        // Get logged-in user info from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences("user_prefs", 0)
        val userName = sharedPreferences.getString("logged_user_name", "Wellness User")
        val userEmail = sharedPreferences.getString("logged_user_email", "user@example.com")

        tvUserName.text = userName
        tvUserEmail.text = userEmail
    }

    private fun setupListeners() {
        // Hydration switch listener
        switchHydration.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkNotificationPermissionAndEnable()
            } else {
                disableHydrationReminder()
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

    private fun checkNotificationPermissionAndEnable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    enableHydrationReminder()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            enableHydrationReminder()
        }
    }

    private fun enableHydrationReminder() {
        val interval = prefsHelper.getReminderInterval()
        hydrationManager.scheduleReminder(interval)
        prefsHelper.setHydrationEnabled(true)

        Toast.makeText(
            requireContext(),
            "Hydration reminders enabled! You'll receive notifications every $interval minutes",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun disableHydrationReminder() {
        hydrationManager.cancelReminder()
        prefsHelper.setHydrationEnabled(false)

        Toast.makeText(
            requireContext(),
            "Hydration reminders disabled",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showIntervalDialog() {
        val intervals = arrayOf(
            "15 minutes",
            "30 minutes",
            "45 minutes",
            "60 minutes",
            "90 minutes",
            "2 hours",
            "3 hours",
            "4 hours"
        )
        val intervalValues = arrayOf(15, 30, 45, 60, 90, 120, 180, 240)

        // Custom ArrayAdapter to style items
        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.select_dialog_item,
            intervals
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.background_dark))
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
                return view
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Select Reminder Interval")
            .setAdapter(adapter) { _, which ->
                val selectedInterval = intervalValues[which]
                tvIntervalValue.text = intervals[which]

                // Save to SharedPreferences
                prefsHelper.setReminderInterval(selectedInterval)

                // Reschedule if enabled
                if (switchHydration.isChecked) {
                    hydrationManager.scheduleReminder(selectedInterval)
                }

                Toast.makeText(
                    requireContext(),
                    "Interval updated to ${intervals[which]}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .create()

        // Set dialog background to dark green
        dialog.window?.setBackgroundDrawableResource(R.color.background_dark)

        // Customize the title text color
        dialog.setOnShowListener {
            val titleId = resources.getIdentifier("alertTitle", "id", "android")
            val titleTextView = dialog.findViewById<TextView>(titleId)
            titleTextView?.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
        }

        dialog.show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("About Wellness Buddy")
            .setMessage(
                "Wellness Buddy v1.0\n\n" +
                        "Your daily health companion to track habits, log moods, and stay hydrated.\n\n" +
                        "Developed as part of IT2010 Mobile Application Development."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Disable hydration reminders on logout
        hydrationManager.cancelReminder()

        // Clear login status
        prefsHelper.setHydrationEnabled(false)

        val sharedPreferences = requireActivity().getSharedPreferences(
            "user_prefs",
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

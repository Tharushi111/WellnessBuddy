package com.example.wellnessbuddy.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.model.MoodEntry
import com.example.wellnessbuddy.adapter.MoodAdapter
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.util.SharedPreferencesHelper
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.wellnessbuddy.AddMoodDialog

class MoodJournalFragment : Fragment() {

    private lateinit var rvMoodEntries: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var emptyStateMood: View

    private lateinit var moodAdapter: MoodAdapter
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var moodsList = mutableListOf<MoodEntry>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_journal, container, false)

        // Get logged-in user email from shared prefs
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val loggedUserEmail = prefs.getString("logged_user_email", "") ?: ""

        // Initialize SharedPreferencesHelper with userEmail
        prefsHelper = SharedPreferencesHelper(requireContext(), loggedUserEmail)

        rvMoodEntries = view.findViewById(R.id.rvMoodEntries)
        fabAddMood = view.findViewById(R.id.fabAddMood)
        emptyStateMood = view.findViewById(R.id.emptyStateMood)

        loadMoods()
        setupRecyclerView()

        fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }

        return view
    }


    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            moods = moodsList,
            onMoodClick = { mood -> showMoodDetails(mood) },
            onDeleteClick = { mood -> showDeleteConfirmation(mood) }
        )

        rvMoodEntries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }

        updateEmptyState()
    }

    private fun loadMoods() {
        moodsList = prefsHelper.loadMoodEntries() // Make sure this returns MutableList<MoodEntry>
    }

    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog(
            context = requireContext(),
            onSave = { newMood: MoodEntry ->
                moodsList.add(0, newMood)
                prefsHelper.addMoodEntry(newMood)
                moodAdapter.addMood(newMood)
                updateEmptyState()

                android.widget.Toast.makeText(
                    requireContext(),
                    "Mood logged successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
        dialog.show()
    }

    private fun showMoodDetails(mood: MoodEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle("${mood.emoji} Mood Entry")
            .setMessage("${mood.note}\n\n${mood.getFormattedDateTime()}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showDeleteConfirmation(mood: MoodEntry) {
        val builder = AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ -> deleteMood(mood) }
            .setNegativeButton("Cancel", null)

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))

            dialog.window?.setBackgroundDrawableResource(R.color.background_dark)

            val titleId = resources.getIdentifier("alertTitle", "id", "android")
            dialog.findViewById<TextView>(titleId)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))

            dialog.findViewById<TextView>(android.R.id.message)
                ?.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
        }

        dialog.show()
    }

    private fun deleteMood(mood: MoodEntry) {
        moodsList.remove(mood)
        prefsHelper.deleteMoodEntry(mood.id)
        moodAdapter.removeMood(mood)
        updateEmptyState()

        android.widget.Toast.makeText(
            requireContext(),
            "Mood entry deleted",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateEmptyState() {
        if (moodsList.isEmpty()) showEmptyState() else hideEmptyState()
    }

    private fun showEmptyState() {
        emptyStateMood.visibility = View.VISIBLE
        rvMoodEntries.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyStateMood.visibility = View.GONE
        rvMoodEntries.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadMoods()
        moodAdapter.updateMoods(moodsList)
        updateEmptyState()
    }
}

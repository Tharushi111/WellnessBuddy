package com.example.wellnessbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

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

        // Initialize SharedPreferences helper
        prefsHelper = SharedPreferencesHelper(requireContext())

        // Initialize views
        rvMoodEntries = view.findViewById(R.id.rvMoodEntries)
        fabAddMood = view.findViewById(R.id.fabAddMood)
        emptyStateMood = view.findViewById(R.id.emptyStateMood)

        // Load moods from SharedPreferences
        loadMoods()

        // Setup RecyclerView
        setupRecyclerView()

        // FAB click listener
        fabAddMood.setOnClickListener {
            showAddMoodDialog()
        }

        return view
    }

    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(
            moods = moodsList,
            onMoodClick = { mood ->
                showMoodDetails(mood)
            },
            onDeleteClick = { mood ->
                showDeleteConfirmation(mood)
            }
        )

        rvMoodEntries.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = moodAdapter
        }

        updateEmptyState()
    }

    private fun loadMoods() {
        moodsList = prefsHelper.loadMoodEntries()
    }

    private fun showAddMoodDialog() {
        val dialog = AddMoodDialog(
            context = requireContext(),
            onSave = { newMood ->
                // Add mood to list and save
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
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood Entry")
            .setMessage("Are you sure you want to delete this mood entry?")
            .setPositiveButton("Delete") { _, _ ->
                deleteMood(mood)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
        if (moodsList.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
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
        // Reload moods when fragment resumes
        loadMoods()
        moodAdapter.updateMoods(moodsList)
        updateEmptyState()
    }
}
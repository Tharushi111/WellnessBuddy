package com.example.wellnessbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MoodJournalFragment : Fragment() {

    private lateinit var rvMoodEntries: RecyclerView
    private lateinit var fabAddMood: FloatingActionButton
    private lateinit var emptyStateMood: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_journal, container, false)

        // Initialize views
        rvMoodEntries = view.findViewById(R.id.rvMoodEntries)
        fabAddMood = view.findViewById(R.id.fabAddMood)
        emptyStateMood = view.findViewById(R.id.emptyStateMood)

        // Setup RecyclerView
        setupRecyclerView()

        // FAB click listener
        fabAddMood.setOnClickListener {
            // TODO: Open dialog to add mood entry
            showAddMoodDialog()
        }

        return view
    }

    private fun setupRecyclerView() {
        rvMoodEntries.layoutManager = LinearLayoutManager(requireContext())

        // TODO: Load mood entries from SharedPreferences and set adapter
        // For now, show empty state
        showEmptyState()
    }

    private fun showAddMoodDialog() {
        // TODO: Implement add mood dialog
        android.widget.Toast.makeText(
            requireContext(),
            "Add Mood feature coming soon!",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun showEmptyState() {
        emptyStateMood.visibility = View.VISIBLE
        rvMoodEntries.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyStateMood.visibility = View.GONE
        rvMoodEntries.visibility = View.VISIBLE
    }
}
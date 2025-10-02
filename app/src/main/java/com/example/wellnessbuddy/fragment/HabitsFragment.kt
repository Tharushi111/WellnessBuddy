package com.example.wellnessbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HabitsFragment : Fragment() {

    private lateinit var rvHabits: RecyclerView
    private lateinit var fabAddHabit: FloatingActionButton
    private lateinit var emptyState: View

    private lateinit var habitAdapter: HabitAdapter
    private lateinit var prefsHelper: SharedPreferencesHelper
    private var habitsList = mutableListOf<Habit>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        // Initialize SharedPreferences helper
        prefsHelper = SharedPreferencesHelper(requireContext())

        // Initialize views
        rvHabits = view.findViewById(R.id.rvHabits)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)
        emptyState = view.findViewById(R.id.emptyState)

        // Load habits from SharedPreferences
        loadHabits()

        // Setup RecyclerView
        setupRecyclerView()

        // FAB click listener
        fabAddHabit.setOnClickListener { showAddHabitDialog() }

        return view
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            habits = habitsList,
            context = requireContext(),
            onHabitClick = { /* Optional: show habit details */ },
            onEditClick = { habit -> showEditHabitDialog(habit) },
            onDeleteClick = { habit -> showDeleteConfirmation(habit) },
            onCompletionChange = { habit, isCompleted ->
                habit.isCompleted = isCompleted
                prefsHelper.updateHabit(habit)
                updateEmptyState()
            }
        )

        rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }

        updateEmptyState()
    }

    private fun loadHabits() {
        habitsList = prefsHelper.loadHabits()
    }

    private fun showAddHabitDialog() {
        val dialog = AddHabitDialog(
            context = requireContext(),
            existingHabit = null,
            onSave = { newHabit ->
                habitsList.add(newHabit)
                prefsHelper.addHabit(newHabit)
                habitAdapter.addHabit(newHabit)
                updateEmptyState()
                android.widget.Toast.makeText(
                    requireContext(),
                    "Habit added successfully!",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
        dialog.show()
    }

    private fun showEditHabitDialog(habit: Habit) {
        val dialog = AddHabitDialog(
            context = requireContext(),
            existingHabit = habit,
            onSave = { updatedHabit ->
                val index = habitsList.indexOfFirst { it.id == updatedHabit.id }
                if (index != -1) {
                    habitsList[index] = updatedHabit
                    prefsHelper.updateHabit(updatedHabit)
                    habitAdapter.updateHabits(habitsList)
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Habit updated successfully!",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        dialog.show()
    }

    private fun showDeleteConfirmation(habit: Habit) {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"?")
            .setPositiveButton("Delete") { _, _ -> deleteHabit(habit) }
            .setNegativeButton("Cancel", null)
            .create()

        // Set dialog background to green
        dialog.window?.setBackgroundDrawableResource(R.color.background_dark)

        dialog.setOnShowListener {
            val positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            val accentGreen = ContextCompat.getColor(requireContext(), R.color.accent_green)
            positive.setTextColor(accentGreen)
            negative.setTextColor(accentGreen)
        }

        dialog.show()
    }


    private fun deleteHabit(habit: Habit) {
        habitsList.remove(habit)
        prefsHelper.deleteHabit(habit.id)
        habitAdapter.removeHabit(habit)
        updateEmptyState()

        android.widget.Toast.makeText(
            requireContext(),
            "Habit deleted",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateEmptyState() {
        if (habitsList.isEmpty()) {
            showEmptyState()
        } else {
            hideEmptyState()
        }
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        rvHabits.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyState.visibility = View.GONE
        rvHabits.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadHabits()
        habitAdapter.updateHabits(habitsList)
        updateEmptyState()
    }
}

package com.example.wellnessbuddy.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellnessbuddy.adapter.HabitAdapter
import com.example.wellnessbuddy.databinding.FragmentHabitsBinding
import com.example.wellnessbuddy.model.Habit
import com.example.wellnessbuddy.model.HabitCompletion
import com.example.wellnessbuddy.util.SharedPreferencesHelper
import com.example.wellnessbuddy.AddHabitDialog
import com.example.wellnessbuddy.DailyProgressDialog
import java.text.SimpleDateFormat
import java.util.*

class HabitsFragment : Fragment() {

    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!

    private lateinit var habitAdapter: HabitAdapter
    private lateinit var prefsHelper: SharedPreferencesHelper

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get logged-in user email from shared prefs
        val prefs = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val loggedUserEmail = prefs.getString("logged_user_email", "") ?: ""

        // Initialize SharedPreferencesHelper with userEmail
        prefsHelper = SharedPreferencesHelper(requireContext(), loggedUserEmail)

        // Load saved habits from SharedPreferences
        val savedHabits = prefsHelper.loadHabits().toMutableList()

        habitAdapter = HabitAdapter(
            savedHabits,
            requireContext(),
            onHabitClick = { habit ->
                Toast.makeText(requireContext(), "Clicked: ${habit.name}", Toast.LENGTH_SHORT).show()
            },
            onEditClick = { habit -> showEditDialog(habit) },
            onDeleteClick = { habit -> showDeleteDialog(habit) },
            onCompletionChange = { habit, isChecked ->
                habit.isCompleted = isChecked
                saveHabitCompletion(habit)
            },
            onChartClick = {
                val dialog = DailyProgressDialog(requireContext(), habitAdapter.getHabits(), prefsHelper)
                dialog.show()
            }
        )

        binding.rvHabits.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = habitAdapter
        }

        binding.fabAddHabit.setOnClickListener { showAddDialog() }
    }

    // ----------------- Dialogs -----------------

    private fun showAddDialog() {
        AddHabitDialog(requireContext()) { newHabit ->
            habitAdapter.addHabit(newHabit)
            prefsHelper.addHabit(newHabit)
            saveHabitCompletion(newHabit)
            Toast.makeText(requireContext(), "Habit added!", Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showEditDialog(habit: Habit) {
        AddHabitDialog(requireContext(), existingHabit = habit) { updatedHabit ->
            updatedHabit.id = habit.id
            habitAdapter.updateHabit(updatedHabit)
            prefsHelper.updateHabit(updatedHabit)
            saveHabitCompletion(updatedHabit)
            Toast.makeText(requireContext(), "Habit updated!", Toast.LENGTH_SHORT).show()
        }.show()
    }

    private fun showDeleteDialog(habit: Habit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete '${habit.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                habitAdapter.removeHabit(habit)
                prefsHelper.deleteHabit(habit.id)
                Toast.makeText(requireContext(), "Habit deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ----------------- Helper Functions -----------------

    private fun saveHabitCompletion(habit: Habit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completion = HabitCompletion(
            habitId = habit.id,
            date = today,
            isCompleted = habit.isCompleted
        )
        prefsHelper.saveCompletionHistory(completion)
        prefsHelper.updateHabit(habit)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

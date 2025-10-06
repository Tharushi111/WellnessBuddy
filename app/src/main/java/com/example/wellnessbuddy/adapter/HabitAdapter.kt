package com.example.wellnessbuddy.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.R
import com.example.wellnessbuddy.model.Habit
import com.google.android.material.checkbox.MaterialCheckBox

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val context: Context,
    private val onHabitClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onCompletionChange: (Habit, Boolean) -> Unit,
    private val onChartClick: () -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitIcon: TextView = itemView.findViewById(R.id.tvHabitIcon)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        val tvHabitTime: TextView = itemView.findViewById(R.id.tvHabitTime)
        val checkboxComplete: MaterialCheckBox = itemView.findViewById(R.id.checkboxComplete)
        val ivChart: ImageView = itemView.findViewById(R.id.ivChart)
        val ivMore: ImageView = itemView.findViewById(R.id.ivMore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.tvHabitIcon.text = habit.icon
        holder.tvHabitName.text = habit.name
        holder.tvHabitTime.text = habit.time

        holder.checkboxComplete.setOnCheckedChangeListener(null)
        holder.checkboxComplete.isChecked = habit.isCompleted
        holder.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            onCompletionChange(habit, isChecked)
        }

        holder.itemView.setOnClickListener { onHabitClick(habit) }
        holder.ivChart.setOnClickListener { onChartClick() }
        holder.ivMore.setOnClickListener { showPopupMenu(holder.ivMore, habit) }
    }

    override fun getItemCount(): Int = habits.size

    private fun showPopupMenu(view: View, habit: Habit) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.inflate(R.menu.habit_item_menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClick(habit)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(habit)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // -------- Utility Functions --------
    fun addHabit(habit: Habit) {
        habits.add(habit)
        notifyItemInserted(habits.size - 1)
    }

    fun updateHabit(updatedHabit: Habit) {
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            notifyItemChanged(index)
        }
    }

    fun removeHabit(habit: Habit) {
        val position = habits.indexOfFirst { it.id == habit.id }
        if (position != -1) {
            habits.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateHabits(newHabits: MutableList<Habit>) {
        habits = newHabits
        notifyDataSetChanged()
    }

    // -------- Getter for habits --------
    fun getHabits(): List<Habit> {
        return habits
    }
}

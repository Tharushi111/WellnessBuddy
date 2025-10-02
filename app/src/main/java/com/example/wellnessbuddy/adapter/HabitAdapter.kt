package com.example.wellnessbuddy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.PopupMenu

class HabitAdapter(
    private var habits: MutableList<Habit>,
    private val context: Context,
    private val onHabitClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit,
    private val onCompletionChange: (Habit, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvHabitIcon: TextView = view.findViewById(R.id.tvHabitIcon)
        val tvHabitName: TextView = view.findViewById(R.id.tvHabitName)
        val tvHabitTime: TextView = view.findViewById(R.id.tvHabitTime)
        val checkboxComplete: CheckBox = view.findViewById(R.id.checkboxComplete)
        val ivMore: ImageView = view.findViewById(R.id.ivMore)
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
        holder.checkboxComplete.isChecked = habit.isCompleted

        // Item click
        holder.itemView.setOnClickListener { onHabitClick(habit) }

        // Checkbox change
        holder.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            onCompletionChange(habit, isChecked)
        }

        // More options menu using PopupMenu
        holder.ivMore.setOnClickListener {
            showPopupMenu(holder, habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    private fun showPopupMenu(holder: HabitViewHolder, habit: Habit) {
        val popup = PopupMenu(context, holder.ivMore)
        popup.menu.add("Edit")
        popup.menu.add("Delete")

        // Optional: force show icons if needed
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val menuPopupHelper = fieldMPopup.get(popup)
            menuPopupHelper.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menuPopupHelper, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.title) {
                "Edit" -> onEditClick(habit)
                "Delete" -> onDeleteClick(habit) // just call fragment's callback
            }
            true
        }
        popup.show()
    }

    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    fun addHabit(habit: Habit) {
        habits.add(habit)
        notifyItemInserted(habits.size - 1)
    }

    fun removeHabit(habit: Habit) {
        val position = habits.indexOf(habit)
        if (position != -1) {
            habits.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}

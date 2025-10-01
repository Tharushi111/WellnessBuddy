package com.example.wellnessbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitAdapter(
    private var habits: MutableList<Habit>,
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

        // Item click listener
        holder.itemView.setOnClickListener {
            onHabitClick(habit)
        }

        // Checkbox change listener
        holder.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
            habit.isCompleted = isChecked
            onCompletionChange(habit, isChecked)
        }

        // More options menu
        holder.ivMore.setOnClickListener { view ->
            showPopupMenu(view, habit)
        }
    }

    override fun getItemCount(): Int = habits.size

    /* Show popup menu with Edit and Delete option */
    private fun showPopupMenu(view: View, habit: Habit) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.habit_item_menu)

        // Force show icons
        try {
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenu)
            menu.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 🔑 Remove default tint so vector icons keep their original colors
        for (i in 0 until popupMenu.menu.size()) {
            val item = popupMenu.menu.getItem(i)
            item.icon?.setTintList(null)
        }

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


    /**
     * Update the habits list
     */
    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }

    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit) {
        habits.add(habit)
        notifyItemInserted(habits.size - 1)
    }

    /**
     * Remove a habit
     */
    fun removeHabit(habit: Habit) {
        val position = habits.indexOf(habit)
        if (position != -1) {
            habits.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
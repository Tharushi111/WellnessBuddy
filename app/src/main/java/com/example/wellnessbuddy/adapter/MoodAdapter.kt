package com.example.wellnessbuddy.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.model.MoodEntry
import com.example.wellnessbuddy.R


class MoodAdapter(
    private var moods: MutableList<MoodEntry>,
    private val onMoodClick: (MoodEntry) -> Unit,
    private val onDeleteClick: (MoodEntry) -> Unit
) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    inner class MoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvMoodEmoji: TextView = view.findViewById(R.id.tvMoodEmoji)
        val tvMoodNote: TextView = view.findViewById(R.id.tvMoodNote)
        val tvMoodDateTime: TextView = view.findViewById(R.id.tvMoodDateTime)
        val ivMoreMood: ImageView = view.findViewById(R.id.ivMoreMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        holder.tvMoodEmoji.text = mood.emoji
        holder.tvMoodNote.text = mood.note
        holder.tvMoodDateTime.text = mood.getFormattedDateTime()

        // Item click listener
        holder.itemView.setOnClickListener {
            onMoodClick(mood)
        }

        // More options menu
        holder.ivMoreMood.setOnClickListener { view ->
            showPopupMenu(view, mood)
        }
    }

    override fun getItemCount(): Int = moods.size

    /**
     * Show popup menu with Delete option
     */
    private fun showPopupMenu(view: View, mood: MoodEntry) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.mood_item_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete_mood -> {
                    onDeleteClick(mood)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    /**
     * Update the moods list
     */
    fun updateMoods(newMoods: List<MoodEntry>) {
        moods.clear()
        moods.addAll(newMoods)
        notifyDataSetChanged()
    }

    /**
     * Add a new mood entry
     */
    fun addMood(mood: MoodEntry) {
        moods.add(0, mood) // Add to beginning
        notifyItemInserted(0)
    }

    /**
     * Remove a mood entry
     */
    fun removeMood(mood: MoodEntry) {
        val position = moods.indexOf(mood)
        if (position != -1) {
            moods.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
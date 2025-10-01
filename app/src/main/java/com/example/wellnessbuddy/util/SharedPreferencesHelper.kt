package com.example.wellnessbuddy

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "wellness_buddy_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_ENABLED = "hydration_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
    }

    // ============ HABITS ============

    /**
     * Save list of habits
     */
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = habits.map { it.toJson() }
        val json = gson.toJson(habitsJson)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    /**
     * Load list of habits
     */
    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return mutableListOf()
        val type = object : TypeToken<List<String>>() {}.type
        val habitsJson: List<String> = gson.fromJson(json, type) ?: return mutableListOf()
        return habitsJson.mapNotNull { Habit.fromJson(it) }.toMutableList()
    }

    /**
     * Add a new habit
     */
    fun addHabit(habit: Habit) {
        val habits = loadHabits()
        habits.add(habit)
        saveHabits(habits)
    }

    /**
     * Update an existing habit
     */
    fun updateHabit(updatedHabit: Habit) {
        val habits = loadHabits()
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits(habits)
        }
    }

    /**
     * Delete a habit
     */
    fun deleteHabit(habitId: String) {
        val habits = loadHabits()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }

    /**
     * Get completion percentage for today
     */
    fun getCompletionPercentage(): Int {
        val habits = loadHabits()
        if (habits.isEmpty()) return 0
        val completedCount = habits.count { it.isCompleted }
        return (completedCount * 100) / habits.size
    }

    // ============ MOOD ENTRIES ============

    /**
     * Save list of mood entries
     */
    fun saveMoodEntries(moods: List<MoodEntry>) {
        val moodsJson = moods.map { it.toJson() }
        val json = gson.toJson(moodsJson)
        prefs.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    /**
     * Load list of mood entries
     */
    fun loadMoodEntries(): MutableList<MoodEntry> {
        val json = prefs.getString(KEY_MOOD_ENTRIES, null) ?: return mutableListOf()
        val type = object : TypeToken<List<String>>() {}.type
        val moodsJson: List<String> = gson.fromJson(json, type) ?: return mutableListOf()
        return moodsJson.mapNotNull { MoodEntry.fromJson(it) }.toMutableList()
    }

    /**
     * Add a new mood entry
     */
    fun addMoodEntry(mood: MoodEntry) {
        val moods = loadMoodEntries()
        moods.add(0, mood) // Add to beginning
        saveMoodEntries(moods)
    }

    /**
     * Delete a mood entry
     */
    fun deleteMoodEntry(moodId: String) {
        val moods = loadMoodEntries()
        moods.removeAll { it.id == moodId }
        saveMoodEntries(moods)
    }

    // ============ SETTINGS ============

    /**
     * Save hydration reminder status
     */
    fun setHydrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HYDRATION_ENABLED, enabled).apply()
    }

    /**
     * Get hydration reminder status
     */
    fun isHydrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_HYDRATION_ENABLED, false)
    }

    /**
     * Save reminder interval
     */
    fun setReminderInterval(minutes: Int) {
        prefs.edit().putInt(KEY_REMINDER_INTERVAL, minutes).apply()
    }

    /**
     * Get reminder interval
     */
    fun getReminderInterval(): Int {
        return prefs.getInt(KEY_REMINDER_INTERVAL, 60)
    }
}
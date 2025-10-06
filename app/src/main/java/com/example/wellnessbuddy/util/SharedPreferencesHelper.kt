package com.example.wellnessbuddy.util

import android.content.Context
import android.content.SharedPreferences
import com.example.wellnessbuddy.model.Habit
import com.example.wellnessbuddy.model.HabitCompletion
import com.example.wellnessbuddy.model.MoodEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesHelper(context: Context, private val userEmail: String) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "wellness_buddy_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_COMPLETION_HISTORY = "completion_history"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_ENABLED = "hydration_enabled"
        private const val KEY_REMINDER_INTERVAL = "reminder_interval"
    }

    // Prefix each key with the user email to isolate data per user
    private fun userKey(key: String) = "${userEmail}_$key"

    // ================= HABITS =================
    fun saveHabits(habits: List<Habit>) {
        val habitsJson = habits.map { it.toJson() }
        prefs.edit().putString(userKey(KEY_HABITS), gson.toJson(habitsJson)).apply()
    }

    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString(userKey(KEY_HABITS), null) ?: return mutableListOf()
        val type = object : TypeToken<List<String>>() {}.type
        val listOfStrings: List<String> = gson.fromJson(json, type) ?: return mutableListOf()
        return listOfStrings.mapNotNull { Habit.fromJson(it) }.toMutableList()
    }

    fun addHabit(habit: Habit) {
        val habits = loadHabits()
        habits.add(habit)
        saveHabits(habits)
    }

    fun updateHabit(updatedHabit: Habit) {
        val habits = loadHabits()
        val index = habits.indexOfFirst { it.id == updatedHabit.id }
        if (index != -1) {
            habits[index] = updatedHabit
            saveHabits(habits)
        }
    }

    fun deleteHabit(habitId: String) {
        val habits = loadHabits()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
        deleteCompletionHistoryForHabit(habitId)
    }

    // ================= COMPLETION HISTORY =================
    fun saveCompletionHistory(completion: HabitCompletion) {
        val history = loadAllCompletionHistory()
        history.removeAll { it.habitId == completion.habitId && it.date == completion.date }
        history.add(completion)
        prefs.edit().putString(userKey(KEY_COMPLETION_HISTORY), gson.toJson(history)).apply()
    }

    fun loadCompletionHistory(habitId: String): List<HabitCompletion> {
        return loadAllCompletionHistory().filter { it.habitId == habitId }
    }

    fun loadAllCompletionHistory(): MutableList<HabitCompletion> {
        val json = prefs.getString(userKey(KEY_COMPLETION_HISTORY), null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<HabitCompletion>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun loadAllCompletionHistoryForDate(date: String): List<HabitCompletion> {
        return loadAllCompletionHistory().filter { it.date == date }
    }

    private fun deleteCompletionHistoryForHabit(habitId: String) {
        val history = loadAllCompletionHistory()
        history.removeAll { it.habitId == habitId }
        prefs.edit().putString(userKey(KEY_COMPLETION_HISTORY), gson.toJson(history)).apply()
    }

    // ================= MOODS =================
    fun loadMoodEntries(): MutableList<MoodEntry> {
        val json = prefs.getString(userKey(KEY_MOOD_ENTRIES), null) ?: return mutableListOf()
        val type = object : TypeToken<List<String>>() {}.type
        val listOfStrings: List<String> = gson.fromJson(json, type) ?: return mutableListOf()
        return listOfStrings.mapNotNull { MoodEntry.fromJson(it) }.toMutableList()
    }

    fun addMoodEntry(mood: MoodEntry) {
        val moods = loadMoodEntries()
        moods.add(0, mood) // latest first
        saveMoodEntries(moods)
    }

    fun deleteMoodEntry(moodId: String) {
        val moods = loadMoodEntries()
        val updated = moods.filter { it.id != moodId }.toMutableList()
        saveMoodEntries(updated)
    }

    private fun saveMoodEntries(moods: MutableList<MoodEntry>) {
        val listOfStrings = moods.map { it.toJson() }
        prefs.edit().putString(userKey(KEY_MOOD_ENTRIES), gson.toJson(listOfStrings)).apply()
    }

    // ================= HYDRATION REMINDER =================
    fun isHydrationEnabled(): Boolean {
        return prefs.getBoolean(userKey(KEY_HYDRATION_ENABLED), false)
    }

    fun setHydrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(userKey(KEY_HYDRATION_ENABLED), enabled).apply()
    }

    fun getReminderInterval(): Int {
        return prefs.getInt(userKey(KEY_REMINDER_INTERVAL), 60) // default 60 minutes
    }

    fun setReminderInterval(minutes: Int) {
        prefs.edit().putInt(userKey(KEY_REMINDER_INTERVAL), minutes).apply()
    }
}

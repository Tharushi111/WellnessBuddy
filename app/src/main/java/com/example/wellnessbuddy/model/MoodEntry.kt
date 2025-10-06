package com.example.wellnessbuddy.model

import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis() // store creation time
) {

    // Returns a readable date-time string for display
    fun getFormattedDateTime(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    // Convert this MoodEntry to JSON string for SharedPreferences
    fun toJson(): String = Gson().toJson(this)

    companion object {
        // Convert JSON string back to MoodEntry
        fun fromJson(json: String): MoodEntry? {
            return try {
                Gson().fromJson(json, MoodEntry::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}

package com.example.wellnessbuddy

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val emoji: String, // 😊, 😢, 😠, 😌, 😰, etc.
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Get formatted date and time string
     */
    fun getFormattedDateTime(): String {
        val date = Date(timestamp)
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 24 * 60 * 60 * 1000 -> { // Less than 24 hours
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Today, ${timeFormat.format(date)}"
            }
            diff < 48 * 60 * 60 * 1000 -> { // Less than 48 hours
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                "Yesterday, ${timeFormat.format(date)}"
            }
            else -> {
                val dateFormat = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
                dateFormat.format(date)
            }
        }
    }

    /**
     * Convert MoodEntry to JSON string for SharedPreferences
     */
    fun toJson(): String {
        return "$id|$emoji|$note|$timestamp"
    }

    companion object {
        /**
         * Create MoodEntry from JSON string
         */
        fun fromJson(json: String): MoodEntry? {
            return try {
                val parts = json.split("|")
                if (parts.size == 4) {
                    MoodEntry(
                        id = parts[0],
                        emoji = parts[1],
                        note = parts[2],
                        timestamp = parts[3].toLong()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
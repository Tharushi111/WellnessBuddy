package com.example.wellnessbuddy

import java.util.UUID

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val icon: String, // Emoji like 💧, 🧘, 🏃, etc.
    val time: String, // Time or description like "8:00 AM" or "Morning"
    var isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Convert Habit to JSON string for SharedPreferences
     */
    fun toJson(): String {
        return "$id|$name|$icon|$time|$isCompleted|$createdAt"
    }

    companion object {
        /**
         * Create Habit from JSON string
         */
        fun fromJson(json: String): Habit? {
            return try {
                val parts = json.split("|")
                if (parts.size == 6) {
                    Habit(
                        id = parts[0],
                        name = parts[1],
                        icon = parts[2],
                        time = parts[3],
                        isCompleted = parts[4].toBoolean(),
                        createdAt = parts[5].toLong()
                    )
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }
}
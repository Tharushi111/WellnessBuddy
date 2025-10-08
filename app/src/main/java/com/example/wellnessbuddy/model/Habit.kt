package com.example.wellnessbuddy.model

import java.util.UUID

data class Habit(
    var id: String = UUID.randomUUID().toString(),
    var name: String,
    var icon: String, // Emoji
    var time: String, // Time or description like "8:00 AM"
    var isCompleted: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
) {
    fun toJson(): String {
        return "$id|$name|$icon|$time|$isCompleted|$createdAt"
    }

    companion object {
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

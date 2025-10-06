package com.example.wellnessbuddy.model

data class HabitCompletion(
    val habitId: String,           // ID of the habit being tracked
    val date: String,              // Format: "yyyy-MM-dd" (e.g., "2025-10-05")
    val isCompleted: Boolean,      // true = completed, false = not completed
    val timestamp: Long = System.currentTimeMillis() // When record was created
)

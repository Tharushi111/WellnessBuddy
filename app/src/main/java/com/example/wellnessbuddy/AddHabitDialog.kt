package com.example.wellnessbuddy

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class AddHabitDialog(
    context: Context,
    private val existingHabit: Habit? = null,
    private val onSave: (Habit) -> Unit
) : Dialog(context) {

    private lateinit var tvDialogTitle: TextView
    private lateinit var gridEmojis: GridLayout
    private lateinit var tvSelectedEmoji: TextView
    private lateinit var etHabitName: TextInputEditText
    private lateinit var etHabitTime: TextInputEditText
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button

    private var selectedEmoji = "💧" // Default emoji

    private val emojiList = listOf(
        "💧", "🧘", "🏃", "💤", "📚", "🍎",
        "🚴", "🎵", "☕", "🌅", "🧹", "💪"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_habit)

        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Initialize views
        tvDialogTitle = findViewById(R.id.tvDialogTitle)
        gridEmojis = findViewById(R.id.gridEmojis)
        tvSelectedEmoji = findViewById(R.id.tvSelectedEmoji)
        etHabitName = findViewById(R.id.etHabitName)
        etHabitTime = findViewById(R.id.etHabitTime)
        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)

        // Setup dialog for edit mode
        if (existingHabit != null) {
            tvDialogTitle.text = "Edit Habit"
            etHabitName.setText(existingHabit.name)
            etHabitTime.setText(existingHabit.time)
            selectedEmoji = existingHabit.icon
            tvSelectedEmoji.text = selectedEmoji
        }

        // Setup emoji grid
        setupEmojiGrid()

        // Button listeners
        btnCancel.setOnClickListener { dismiss() }
        btnSave.setOnClickListener { saveHabit() }
    }

    private fun setupEmojiGrid() {
        emojiList.forEach { emoji ->
            val emojiView = TextView(context).apply {
                text = emoji
                textSize = 28f
                setPadding(16, 16, 16, 16)
                setOnClickListener {
                    selectedEmoji = emoji
                    tvSelectedEmoji.text = emoji
                }
            }
            gridEmojis.addView(emojiView)
        }
    }

    private fun saveHabit() {
        val name = etHabitName.text.toString().trim()
        val time = etHabitTime.text.toString().trim()

        // Validation
        if (name.isEmpty()) {
            Toast.makeText(context, "Please enter habit name", Toast.LENGTH_SHORT).show()
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(context, "Please enter time or description", Toast.LENGTH_SHORT).show()
            return
        }

        // Create or update habit
        val habit = if (existingHabit != null) {
            existingHabit.copy(
                name = name,
                icon = selectedEmoji,
                time = time
            )
        } else {
            Habit(
                name = name,
                icon = selectedEmoji,
                time = time
            )
        }

        onSave(habit)
        dismiss()
    }
}
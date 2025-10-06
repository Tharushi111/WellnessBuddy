package com.example.wellnessbuddy

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import com.example.wellnessbuddy.model.Habit
import com.example.wellnessbuddy.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class AddHabitDialog(
    context: Context,
    private val existingHabit: Habit? = null, // For editing
    private val onHabitSaved: (Habit) -> Unit
) : Dialog(context) {

    private lateinit var etHabitName: TextInputEditText
    private lateinit var etHabitTime: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var tvTitle: TextView
    private lateinit var tvSelectedEmoji: TextView
    private lateinit var gridEmojis: GridLayout

    // Emojis list
    private val emojiList = listOf(
        "💧","🏃","🏋️‍♀️","🚴","🧘","🛀","🍎","🥦","🍇","🍌",
        "☕","🛏","📚","✍️","🖌️","🎨","🎵","🎧","🎹","🎸",
        "📝","💪","🧠","🌿","🛍️","💤","🍫","🍪","🍵","🍶",
        "📖","🧩","⚽","🏀","🏐","🎯","⏰","🗓️","🧴","🛋️"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_habit)
        setCancelable(true)

        window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initViews()
        setupEmojiGrid()
        setupInitialData()
        setupListeners()
    }

    private fun initViews() {
        etHabitName = findViewById(R.id.etHabitName)
        etHabitTime = findViewById(R.id.etHabitTime)
        btnSave = findViewById(R.id.btnSave)
        btnCancel = findViewById(R.id.btnCancel)
        tvTitle = findViewById(R.id.tvDialogTitle)
        tvSelectedEmoji = findViewById(R.id.tvSelectedEmoji)
        gridEmojis = findViewById(R.id.gridEmojis)
    }

    private fun setupEmojiGrid() {
        gridEmojis.removeAllViews()
        emojiList.forEach { emoji ->
            val textView = TextView(context).apply {
                text = emoji
                textSize = 28f
                gravity = Gravity.CENTER
                setPadding(16, 16, 16, 16)
                setOnClickListener { tvSelectedEmoji.text = emoji }
            }
            val layoutParams = GridLayout.LayoutParams().apply {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
                height = ViewGroup.LayoutParams.WRAP_CONTENT
                setMargins(8, 8, 8, 8)
            }
            gridEmojis.addView(textView, layoutParams)
        }
    }

    private fun setupInitialData() {
        existingHabit?.let {
            etHabitName.setText(it.name)
            etHabitTime.setText(it.time)
            tvSelectedEmoji.text = it.icon
            tvTitle.text = "Edit Habit"
            btnSave.text = "Update"
        } ?: run {
            tvTitle.text = "Add Habit"
            btnSave.text = "Save"
            tvSelectedEmoji.text = ""
        }
    }

    private fun setupListeners() {
        // Show TimePicker when time input clicked
        etHabitTime.setOnClickListener {
            showTimePicker()
        }

        btnSave.setOnClickListener {
            val name = etHabitName.text.toString().trim()
            val time = etHabitTime.text.toString().trim()
            val icon = tvSelectedEmoji.text.toString().trim()

            // Validation
            when {
                name.isEmpty() -> {
                    Toast.makeText(context, "Please enter habit name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                time.isEmpty() -> {
                    Toast.makeText(context, "Please select habit time", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                icon.isEmpty() -> {
                    Toast.makeText(context, "Please select an emoji", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            // Create or update habit
            val habit = existingHabit?.copy(
                name = name,
                icon = icon,
                time = time
            ) ?: Habit(
                id = UUID.randomUUID().toString(),
                name = name,
                icon = icon,
                time = time
            )

            onHabitSaved(habit)
            dismiss()
        }

        btnCancel.setOnClickListener { dismiss() }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            etHabitTime.setText(formattedTime)
        }, hour, minute, true)

        timePicker.show()
    }
}

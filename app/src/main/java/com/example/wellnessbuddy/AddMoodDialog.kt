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

class AddMoodDialog(
    context: Context,
    private val onSave: (MoodEntry) -> Unit
) : Dialog(context) {

    private lateinit var gridMoodEmojis: GridLayout
    private lateinit var tvSelectedMoodEmoji: TextView
    private lateinit var tvMoodName: TextView
    private lateinit var tvMoodDescription: TextView
    private lateinit var etMoodNote: TextInputEditText
    private lateinit var btnCancelMood: Button
    private lateinit var btnSaveMood: Button

    private var selectedEmoji = "😊" // Default emoji
    private var selectedMoodName = "Happy"
    private var selectedMoodDesc = "Feeling joyful and content"

    // Mood data: Emoji, Name, Description
    private val moodList = listOf(
        MoodData("😊", "Happy", "Feeling joyful and content"),
        MoodData("😄", "Excited", "Full of energy and enthusiasm"),
        MoodData("😌", "Calm", "Peaceful and relaxed"),
        MoodData("😢", "Sad", "Feeling down or upset"),
        MoodData("😠", "Angry", "Frustrated or irritated"),
        MoodData("😰", "Anxious", "Worried or nervous"),
        MoodData("😴", "Tired", "Exhausted or sleepy"),
        MoodData("🤒", "Sick", "Not feeling well physically"),
        MoodData("😎", "Confident", "Feeling self-assured"),
        MoodData("🥰", "Loved", "Feeling cared for and appreciated")
    )

    data class MoodData(val emoji: String, val name: String, val description: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_add_mood)

        // Set dialog width
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Initialize views
        gridMoodEmojis = findViewById(R.id.gridMoodEmojis)
        tvSelectedMoodEmoji = findViewById(R.id.tvSelectedMoodEmoji)
        tvMoodName = findViewById(R.id.tvMoodName)
        tvMoodDescription = findViewById(R.id.tvMoodDescription)
        etMoodNote = findViewById(R.id.etMoodNote)
        btnCancelMood = findViewById(R.id.btnCancelMood)
        btnSaveMood = findViewById(R.id.btnSaveMood)

        // Setup mood grid
        setupMoodGrid()

        // Button listeners
        btnCancelMood.setOnClickListener { dismiss() }
        btnSaveMood.setOnClickListener { saveMood() }
    }

    private fun setupMoodGrid() {
        moodList.forEach { moodData ->
            val emojiView = TextView(context).apply {
                text = moodData.emoji
                textSize = 36f
                setPadding(20, 20, 20, 20)
                setOnClickListener {
                    selectMood(moodData)
                }
            }
            gridMoodEmojis.addView(emojiView)
        }
    }

    private fun selectMood(moodData: MoodData) {
        selectedEmoji = moodData.emoji
        selectedMoodName = moodData.name
        selectedMoodDesc = moodData.description

        tvSelectedMoodEmoji.text = selectedEmoji
        tvMoodName.text = selectedMoodName
        tvMoodDescription.text = selectedMoodDesc

        // Animate selection
        tvSelectedMoodEmoji.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction {
                tvSelectedMoodEmoji.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }

    private fun saveMood() {
        val note = etMoodNote.text.toString().trim()

        // Validation
        if (note.isEmpty()) {
            // Allow empty note, but use mood name as default
            val mood = MoodEntry(
                emoji = selectedEmoji,
                note = selectedMoodName
            )
            onSave(mood)
            dismiss()
            return
        }

        // Create mood entry
        val mood = MoodEntry(
            emoji = selectedEmoji,
            note = note
        )

        onSave(mood)
        dismiss()
    }
}
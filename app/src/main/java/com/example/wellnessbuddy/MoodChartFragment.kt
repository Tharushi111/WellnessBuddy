package com.example.wellnessbuddy

import com.example.wellnessbuddy.util.SharedPreferencesHelper
import com.example.wellnessbuddy.model.MoodEntry
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*


class MoodChartFragment : Fragment() {

    private lateinit var lineChart: LineChart
    private lateinit var emptyState: LinearLayout
    private lateinit var prefsHelper: SharedPreferencesHelper

    // Mood scale from worst to best (1-10)
    private val moodValues = mapOf(
        "🤒" to 1f,   // Sick
        "😠" to 2f,   // Angry
        "😢" to 3f,   // Sad
        "😰" to 4f,   // Anxious
        "😴" to 5f,   // Tired
        "😐" to 6f,   // Neutral
        "😌" to 7f,   // Calm
        "😊" to 8f,   // Happy
        "😄" to 9f,   // Very Happy
        "🥰" to 10f   // Loved/Best
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mood_chart, container, false)
        lineChart = view.findViewById(R.id.lineChart)
        emptyState = view.findViewById(R.id.emptyStateChart)

        // Get logged-in user email from shared prefs
        val prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val loggedUserEmail = prefs.getString("logged_user_email", "") ?: ""

        // Initialize SharedPreferencesHelper with userEmail
        prefsHelper = SharedPreferencesHelper(requireContext(), loggedUserEmail)

        loadMoodData()
        return view
    }



    private fun loadMoodData() {
        val moodEntries = prefsHelper.loadMoodEntries()

        if (moodEntries.isEmpty()) {
            showEmptyState()
            return
        }

        hideEmptyState()

        // Get last 7 days data
        val entries = getLast7DaysMoodData(moodEntries)

        if (entries.isEmpty()) {
            showEmptyState()
            return
        }

        // Create beautiful dataset
        val dataSet = LineDataSet(entries, "Your Mood Journey").apply {
            // Line styling
            color = ContextCompat.getColor(requireContext(), R.color.accent_green)
            lineWidth = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            cubicIntensity = 0.15f

            // Circle (data points) styling
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.accent_green))
            circleRadius = 7f
            circleHoleRadius = 4f
            setDrawCircleHole(true)
            circleHoleColor = ContextCompat.getColor(requireContext(), R.color.surface_dark)

            // Values on points
            setDrawValues(false)

            // Fill gradient under line
            setDrawFilled(true)
            val gradientColors = intArrayOf(
                Color.parseColor("#804CAF50"), // Semi-transparent green
                Color.TRANSPARENT
            )
            fillDrawable = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                gradientColors
            )

            // Highlight styling
            highLightColor = Color.parseColor("#FF9800")
            setDrawHighlightIndicators(true)
        }

        lineChart.data = LineData(dataSet)
        configureChartAppearance()
        lineChart.animateXY(1200, 1200)
        lineChart.invalidate()
    }

    private fun getLast7DaysMoodData(moodEntries: List<MoodEntry>): List<Entry> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val entries = mutableListOf<Entry>()

        // Group moods by date and calculate average
        val moodsByDate = mutableMapOf<String, MutableList<Float>>()

        moodEntries.forEach { mood ->
            val date = dateFormat.format(Date(mood.timestamp))
            val value = moodValues[mood.emoji] ?: 5f
            moodsByDate.getOrPut(date) { mutableListOf() }.add(value)
        }

        // Create entries for last 7 days
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val date = dateFormat.format(calendar.time)

            val avgMood = moodsByDate[date]?.average()?.toFloat() ?: 0f
            entries.add(Entry((6 - i).toFloat(), avgMood))
        }

        return entries
    }

    private fun configureChartAppearance() {
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            extraBottomOffset = 15f
            extraTopOffset = 15f
            extraLeftOffset = 10f
            extraRightOffset = 10f

            // X-axis (Days)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                textSize = 12f
                setDrawGridLines(false)
                setDrawAxisLine(true)
                axisLineColor = ContextCompat.getColor(requireContext(), R.color.text_hint)
                axisLineWidth = 1f
                granularity = 1f
                labelCount = 7
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_YEAR, -6 + value.toInt())
                        // Always show day name (Mon, Tue, Wed, etc.)
                        return SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
                    }
                }
            }

            // Y-axis (Mood emojis)
            axisLeft.apply {
                textColor = ContextCompat.getColor(requireContext(), R.color.text_primary)
                textSize = 16f
                setDrawGridLines(true)
                gridColor = Color.parseColor("#2E2E2E")
                gridLineWidth = 1f
                enableGridDashedLine(10f, 10f, 0f)
                setDrawAxisLine(false)
                axisMinimum = 0f
                axisMaximum = 11f
                labelCount = 11
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return when (value.toInt()) {
                            10 -> "🥰"
                            9 -> "😄"
                            8 -> "😊"
                            7 -> "😌"
                            6 -> "😐"
                            5 -> "😴"
                            4 -> "😰"
                            3 -> "😢"
                            2 -> "😠"
                            1 -> "🤒"
                            else -> ""
                        }
                    }
                }
            }

            axisRight.isEnabled = false

            // Legend
            legend.apply {
                isEnabled = true
                textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
                textSize = 12f
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                form = Legend.LegendForm.LINE
                formSize = 12f
                xEntrySpace = 10f
                yOffset = 5f
            }
        }
    }

    private fun showEmptyState() {
        emptyState.visibility = View.VISIBLE
        lineChart.visibility = View.GONE
    }

    private fun hideEmptyState() {
        emptyState.visibility = View.GONE
        lineChart.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        loadMoodData()
    }
}
package com.example.wellnessbuddy

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnessbuddy.model.Habit
import com.example.wellnessbuddy.util.SharedPreferencesHelper
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.text.SimpleDateFormat
import java.util.*

class DailyProgressDialog(
    context: Context,
    private val allHabits: List<Habit>,
    private val prefsHelper: SharedPreferencesHelper
) : Dialog(context) {

    private lateinit var ivClose: ImageView
    private lateinit var tvTodayDate: TextView
    private lateinit var circularProgress: CircularProgressIndicator
    private lateinit var tvProgressPercentage: TextView
    private lateinit var tvCompletedCount: TextView
    private lateinit var tvPendingCount: TextView
    private lateinit var barChart: BarChart
    private lateinit var rvDailyHabits: RecyclerView

    private lateinit var adapter: DailyHabitsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_daily_progress)

        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        initViews()
        setupCloseButton()
        setupHabitsList()
        updateProgress()
    }

    private fun initViews() {
        ivClose = findViewById(R.id.ivClose)
        tvTodayDate = findViewById(R.id.tvTodayDate)
        circularProgress = findViewById(R.id.circularProgress)
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage)
        tvCompletedCount = findViewById(R.id.tvCompletedCount)
        tvPendingCount = findViewById(R.id.tvPendingCount)
        barChart = findViewById(R.id.barChart)
        rvDailyHabits = findViewById(R.id.rvDailyHabits)
    }

    private fun setupCloseButton() {
        ivClose.setOnClickListener { dismiss() }
    }

    fun updateProgress() {
        // Update date
        val dateFormat = SimpleDateFormat("EEEE - MMMM dd, yyyy", Locale.getDefault())
        tvTodayDate.text = "Today - ${dateFormat.format(Date())}"

        // Calculate completed/pending counts
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val completionHistory = prefsHelper.loadAllCompletionHistoryForDate(today)
        val completedCount = completionHistory.count { it.isCompleted }
        val pendingCount = allHabits.size - completedCount
        val progressPercentage = if (allHabits.isNotEmpty()) (completedCount * 100) / allHabits.size else 0

        // Animate circular progress
        animateCircularProgress(progressPercentage)

        // Animate counts
        animateCount(tvCompletedCount, 0, completedCount)
        animateCount(tvPendingCount, 0, pendingCount)

        // Update chart
        setupWeeklyChart()

        // Refresh RecyclerView
        adapter.updateHabits(allHabits)
    }

    private fun animateCircularProgress(targetProgress: Int) {
        val animator = ValueAnimator.ofInt(0, targetProgress)
        animator.duration = 800
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val progress = animation.animatedValue as Int
            circularProgress.setProgressCompat(progress, true)
            tvProgressPercentage.text = "$progress%"
        }
        animator.start()
    }

    private fun animateCount(textView: TextView, start: Int, end: Int) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 500
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = (animation.animatedValue as Int).toString()
        }
        animator.start()
    }

    private fun setupWeeklyChart() {
        val weekData = getLast7DaysData()
        val completedEntries = ArrayList<BarEntry>()
        val pendingEntries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        weekData.forEachIndexed { index, dayData ->
            completedEntries.add(BarEntry(index.toFloat(), dayData.completed.toFloat()))
            pendingEntries.add(BarEntry(index.toFloat(), dayData.pending.toFloat()))
            labels.add(dayData.dayLabel)
        }

        val completedDataSet = BarDataSet(completedEntries, "Completed").apply {
            color = Color.parseColor("#4CAF50")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        val pendingDataSet = BarDataSet(pendingEntries, "Pending").apply {
            color = Color.parseColor("#FF9800")
            valueTextColor = Color.WHITE
            valueTextSize = 10f
        }

        val barWidth = 0.35f
        val barSpace = 0.03f
        val groupSpace = 0.3f

        val barData = BarData(completedDataSet, pendingDataSet).apply { this.barWidth = barWidth }

        barChart.apply {
            data = barData
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setScaleEnabled(false)
            setPinchZoom(false)
            legend.textColor = Color.parseColor("#9E9E9E")
            legend.textSize = 12f
            setBackgroundColor(Color.parseColor("#2C2C2C"))

            // X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(labels)
                textColor = Color.parseColor("#9E9E9E")
                textSize = 11f
                setCenterAxisLabels(true)
                axisMinimum = 0f
                axisMaximum = 0f + getGroupWidth(barWidth, barSpace, groupSpace, barData.dataSetCount) * weekData.size
            }

            // Y-axis
            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = Color.parseColor("#3E3E3E")
                textColor = Color.parseColor("#9E9E9E")
                axisMinimum = 0f
                granularity = 1f
            }
            axisRight.isEnabled = false

            groupBars(0f, groupSpace, barSpace)
            animateY(800)
            invalidate()
        }
    }

    private fun getGroupWidth(barWidth: Float, barSpace: Float, groupSpace: Float, dataSetCount: Int): Float {
        return dataSetCount * (barWidth + barSpace) + groupSpace
    }

    private fun getLast7DaysData(): List<DayData> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dataList = mutableListOf<DayData>()

        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateString = dateFormat.format(calendar.time)
            val dayLabel = if (i == 0) "Today" else dayFormat.format(calendar.time)

            val completionHistory = prefsHelper.loadAllCompletionHistoryForDate(dateString)
            val completedCount = completionHistory.count { it.isCompleted }
            val pendingCount = allHabits.size - completedCount

            dataList.add(DayData(dayLabel, completedCount, pendingCount))
        }

        return dataList
    }

    private fun setupHabitsList() {
        adapter = DailyHabitsAdapter(allHabits.toMutableList())
        rvDailyHabits.layoutManager = LinearLayoutManager(context)
        rvDailyHabits.adapter = adapter
    }

    data class DayData(
        val dayLabel: String,
        val completed: Int,
        val pending: Int
    )
}

// Adapter for daily habits list
class DailyHabitsAdapter(
    private var habits: MutableList<Habit>
) : RecyclerView.Adapter<DailyHabitsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvHabitIcon: TextView = itemView.findViewById(R.id.tvHabitIconDaily)
        val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitNameDaily)
        val tvStatusIcon: TextView = itemView.findViewById(R.id.tvStatusIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_habit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val habit = habits[position]
        holder.tvHabitIcon.text = habit.icon
        holder.tvHabitName.text = habit.name

        if (habit.isCompleted) {
            holder.tvStatusIcon.text = "✓"
            holder.tvStatusIcon.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            holder.tvStatusIcon.text = "○"
            holder.tvStatusIcon.setTextColor(Color.parseColor("#FF9800"))
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        habits.clear()
        habits.addAll(newHabits)
        notifyDataSetChanged()
    }
}

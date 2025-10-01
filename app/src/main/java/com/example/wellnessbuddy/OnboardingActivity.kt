package com.example.wellnessbuddy

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var btnGetStarted: Button
    private lateinit var btnNext: Button
    private lateinit var tvSkip: TextView
    private lateinit var adapter: OnboardingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Hide action bar
        supportActionBar?.hide()

        // Initialize views
        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)
        btnGetStarted = findViewById(R.id.btnGetStarted)
        btnNext = findViewById(R.id.btnNext)
        tvSkip = findViewById(R.id.tvSkip)

        // Setup adapter
        adapter = OnboardingAdapter(getOnboardingItems())
        viewPager.adapter = adapter

        // Setup indicators
        setupIndicators()
        setCurrentIndicator(0)

        // ViewPager change listener
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)

                if (position == adapter.itemCount - 1) {
                    // Last page → Show Get Started only
                    btnGetStarted.visibility = View.VISIBLE
                    btnNext.visibility = View.GONE
                    tvSkip.visibility = View.GONE
                } else {
                    // Middle pages → Show Skip + Next
                    btnGetStarted.visibility = View.GONE
                    btnNext.visibility = View.VISIBLE
                    tvSkip.visibility = View.VISIBLE
                }
            }
        })

        // Next button click
        btnNext.setOnClickListener {
            val nextItem = viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                viewPager.currentItem = nextItem
            } else {
                completeOnboarding()
            }
        }

        // Get Started button click
        btnGetStarted.setOnClickListener {
            completeOnboarding()
        }

        // Skip button click
        tvSkip.setOnClickListener {
            completeOnboarding()
        }
    }

    private fun getOnboardingItems(): List<OnboardingItem> {
        return listOf(
            OnboardingItem(
                R.drawable.onboarding1,
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_description_1)
            ),
            OnboardingItem(
                R.drawable.onboarding2,
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_description_2)
            )
        )
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<View>(adapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = View(this)
            indicators[i]?.apply {
                layoutParams.width = 25
                layoutParams.height = 25
                setBackgroundResource(R.drawable.indicator_inactive)
                this.layoutParams = layoutParams
            }
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = indicatorLayout.childCount
        for (i in 0 until childCount) {
            val imageView = indicatorLayout.getChildAt(i)
            if (i == position) {
                imageView.setBackgroundResource(R.drawable.indicator_active)
            } else {
                imageView.setBackgroundResource(R.drawable.indicator_inactive)
            }
        }
    }

    private fun completeOnboarding() {
        // Save onboarding complete status
        val sharedPreferences = getSharedPreferences(
            getString(R.string.pref_name),
            MODE_PRIVATE
        )
        sharedPreferences.edit()
            .putBoolean(getString(R.string.pref_onboarding_complete), true)
            .apply()

        // Navigate to MainActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}

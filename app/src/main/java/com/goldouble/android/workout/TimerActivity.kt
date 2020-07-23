package com.goldouble.android.workout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.goldouble.android.workout.adapter.TimerViewPagerAdapter
import kotlinx.android.synthetic.main.activity_timer.*

class TimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerViewPager.adapter = TimerViewPagerAdapter(this)
        timerViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }
}
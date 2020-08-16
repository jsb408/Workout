package com.goldouble.android.workout

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.goldouble.android.workout.adapter.TimerViewPagerAdapter
import com.goldouble.android.workout.customView.CustomActionbar
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.*

class TimerActivity : AppCompatActivity() {
    var mTimer: Timer? = null
    lateinit var customActionbar: CustomActionbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        customActionbar = CustomActionbar(this).apply {
            setToolbar()
            setTitle(R.string.main_timerBtnLbl)
        }

        timerViewPager.adapter = TimerViewPagerAdapter(this)
        timerViewPager.apply {
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    val titles = arrayOf(R.string.main_timerBtnLbl, R.string.timer_curRecordLbl)
                    customActionbar.setTitle(titles[position])
                }
            })
        }
        TabLayoutMediator(indicator, timerViewPager) { _, _ -> Unit}.attach()
    }

    override fun onResume() {
        super.onResume()
        timerViewPager.adapter = TimerViewPagerAdapter(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_timer, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
            R.id.set_rest_time -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }
}
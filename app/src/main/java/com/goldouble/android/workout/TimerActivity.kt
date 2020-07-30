package com.goldouble.android.workout

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.viewpager2.widget.ViewPager2
import com.goldouble.android.workout.adapter.TimerViewPagerAdapter
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.activity_timer_timer.*
import kotlinx.android.synthetic.main.dialog_number_picker.view.*
import java.util.*

class TimerActivity : AppCompatActivity() {
    var rsTime = 60
    var mTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        timerViewPager.adapter = TimerViewPagerAdapter(this)
        timerViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.main_timerBtnLbl)
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
                val numberPickerDialog = layoutInflater.inflate(R.layout.dialog_number_picker, null).apply {
                    numberPicker.maxValue = 10
                    numberPicker.minValue = 1
                    numberPicker.value = rsTime / 60
                }

                AlertDialog.Builder(this).setView(numberPickerDialog)
                    .setPositiveButton(R.string.submit) { _, _ ->
                        val selectedTime = numberPickerDialog.numberPicker.value
                        val restTimeText = "${getString(R.string.rest)} ${selectedTime}${getString(R.string.minute)}"
                        restTimeBtn.text = restTimeText
                        rsTime = selectedTime * 60
                    }.create().apply {
                        setOnShowListener {
                            getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent))
                        }
                        show()
                    }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        mTimer?.cancel()
    }
}
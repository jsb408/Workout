package com.goldouble.android.workout

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_bedtime.*
import java.text.DecimalFormat
import java.time.LocalDateTime

class BedtimeActivity : AppCompatActivity() {
    val MINUTE_OF_DAY = 1440
    val timeTextList by lazy { listOf(timeText1, timeText2, timeText3) }

    var hour = 7
    var minute = 0

    var isSleep = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bedtime)

        hourPicker.apply {
            maxValue = 23
            minValue = 0
            value = hour
            setOnValueChangedListener { _, _, newVal ->
                hour = newVal
                calculateTime()
            }
        }

        minutePicker.apply {
            maxValue = 11
            minValue = 0
            setFormatter { DecimalFormat("00").format(it * 5) }
            setOnValueChangedListener { _, _, newVal ->
                minute = newVal * 5
                calculateTime()
            }
        }

        timeTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.sleepTimeRadio -> {
                    bedtimeLbl.text = getString(R.string.main_sleepBtnLbl)
                    infoText1Lbl.text = getString(R.string.bedtime_haveto1)
                    infoText2Lbl.text = getString(R.string.bedtime_haveto2)
                    isSleep = true
                }

                R.id.wakeupTimeRadio -> {
                    bedtimeLbl.text = getString(R.string.wakeup_time)
                    infoText1Lbl.text = getString(R.string.bedtime_planto1)
                    infoText2Lbl.text = getString(R.string.bedtime_planto2)
                    isSleep = false
                }
            }
            calculateTime()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.main_sleepBtnLbl)

        calculateTime()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun calculateTime() {
        val decimalFormat = DecimalFormat("00")
        val times = if(isSleep) List(3) { i -> (hour * 60 + minute) - 90 * (i + 3) }
                                else List(3) { i -> (hour * 60 + minute) + 90 * (i + 4)}

        times.forEachIndexed { i, value ->
            val time = if (value < 0) value + MINUTE_OF_DAY else if (value >= MINUTE_OF_DAY) value - MINUTE_OF_DAY else value
            val timeText = "${decimalFormat.format(time / 60)}:${decimalFormat.format(time % 60)}"

            timeTextList[i].text = timeText
        }
    }
}
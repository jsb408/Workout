package com.goldouble.android.workout

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.ThemeUtils
import com.goldouble.android.workout.customView.CustomActionbar
import kotlinx.android.synthetic.main.activity_bedtime.*
import java.text.DecimalFormat

class BedtimeActivity : AppCompatActivity() {
    val MINUTE_OF_DAY = 1440
    val timeTextList by lazy { listOf(timeText1, timeText2, timeText3) }

    var hour = 7
    var minute = 0

    var isSleep = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bedtime)

        val customActionbar = CustomActionbar(this).apply {
            setToolbar()
            setTitle(R.string.main_sleepBtnLbl)
        }

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

        timeTypeSwitch.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId) {
                ObjectAnimator.ofObject(bedtimeConstraintLayout, "backgroundColor", ArgbEvaluator(), getColor(R.color.nightBackground), getColor(R.color.background))
                    .apply {
                        duration = 1500
                        start()
                    }
                ObjectAnimator.ofObject(infoText1Lbl, "textColor", ArgbEvaluator(), Color.WHITE, getColor(R.color.textColor))
                    .apply {
                        duration = 1500
                        start()
                    }
                ObjectAnimator.ofObject(infoText2Lbl, "textColor", ArgbEvaluator(), Color.WHITE, getColor(R.color.textColor))
                    .apply {
                        duration = 1500
                        start()
                    }
                infoText1Lbl.text = getString(R.string.bedtime_planto1)
                infoText2Lbl.text = getString(R.string.bedtime_planto2)
                customActionbar.setTitle(R.string.wakeup_time)
            } else {
                ObjectAnimator.ofObject( bedtimeConstraintLayout, "backgroundColor", ArgbEvaluator(), getColor(R.color.background), getColor(R.color.nightBackground))
                    .apply {
                        duration = 1500
                        start()
                    }
                ObjectAnimator.ofObject(infoText1Lbl, "textColor", ArgbEvaluator(), getColor(R.color.textColor), Color.WHITE)
                    .apply {
                        duration = 1500
                        start()
                    }
                ObjectAnimator.ofObject(infoText2Lbl, "textColor", ArgbEvaluator(), getColor(R.color.textColor), Color.WHITE)
                    .apply {
                        duration = 1500
                        start()
                    }
                infoText1Lbl.text = getString(R.string.bedtime_haveto1)
                infoText2Lbl.text = getString(R.string.bedtime_haveto2)
                customActionbar.setTitle(R.string.sleep_time)
            }
            isSleep = !checkedId
            calculateTime()
        }

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
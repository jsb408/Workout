package com.goldouble.android.workout

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.goldouble.android.workout.customView.CustomActionbar
import kotlinx.android.synthetic.main.actionbar_custom.view.*
import kotlinx.android.synthetic.main.activity_bedtime.*
import java.text.DecimalFormat

class BedtimeActivity : AppCompatActivity() {
    val MINUTE_OF_DAY = 1440
    val timeTextList by lazy { listOf(timeText1, timeText2, timeText3) }

    val ANIMATE_TIME = 700L

    var hour = 7
    var minute = 0

    var isSleep = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bedtime)

        val customActionbar = CustomActionbar(this).apply {
            setToolbar()
            setTitle(R.string.main_sleepBtnLbl)
            setBackgroundColor(getColor(R.color.nightCardBackground))
            setTextColor(Color.WHITE)
        }

        hourPicker.apply {
            maxValue = 23
            minValue = 0
            value = hour
            setOnValueChangedListener { _, _, newVal ->
                hour = newVal
                calculateTime()
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                textColor = Color.WHITE
            }
        }

        timeTypeSwitch.setOnCheckedChangeListener { _, checkedId ->
            if(checkedId) {
                animateColor(bedtimeConstraintLayout, "backgroundColor", getColor(R.color.nightBackground), getColor(R.color.background))
                animateColor(infoText1Lbl, "textColor", Color.WHITE, getColor(R.color.textColor))
                animateColor(infoText2Lbl, "textColor", Color.WHITE, getColor(R.color.textColor))

                animateColor(timePickerRelative, "backgroundColor", getColor(R.color.nightCardBackground), Color.WHITE)
                animateColor(timeTextRelative, "backgroundColor", getColor(R.color.nightCardBackground), Color.WHITE)

                animateColor(timeDivider, "textColor", Color.WHITE, getColor(R.color.textColor))

                animateColor(toolbar.toolbarLayout, "backgroundColor", getColor(R.color.nightCardBackground), Color.WHITE)
                animateColor(toolbar.titleText, "textColor", Color.WHITE, getColor(R.color.textColor))
                animateColor(toolbar.backButton, "colorFilter", Color.WHITE, getColor(R.color.textColor))

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    animateColor(hourPicker, "textColor", Color.WHITE, getColor(R.color.textColor))
                    animateColor(minutePicker,"textColor", Color.WHITE, getColor(R.color.textColor))
                }

                infoText1Lbl.text = getString(R.string.bedtime_planto1)
                infoText2Lbl.text = getString(R.string.bedtime_planto2)
                customActionbar.setTitle(R.string.wakeup_time)
            } else {
                animateColor(bedtimeConstraintLayout, "backgroundColor", getColor(R.color.background), getColor(R.color.nightBackground))
                animateColor(infoText1Lbl, "textColor", getColor(R.color.textColor), Color.WHITE)
                animateColor(infoText2Lbl, "textColor", getColor(R.color.textColor), Color.WHITE)

                animateColor(timePickerRelative, "backgroundColor", Color.WHITE, getColor(R.color.nightCardBackground))
                animateColor(timeTextRelative, "backgroundColor", Color.WHITE, getColor(R.color.nightCardBackground))

                animateColor(timeDivider, "textColor", getColor(R.color.textColor), Color.WHITE)

                animateColor(toolbar.toolbarLayout, "backgroundColor", Color.WHITE, getColor(R.color.nightCardBackground))
                animateColor(toolbar.titleText, "textColor", getColor(R.color.textColor), Color.WHITE)
                animateColor(toolbar.backButton, "colorFilter", getColor(R.color.textColor), Color.WHITE)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    animateColor(hourPicker, "textColor", getColor(R.color.textColor), Color.WHITE)
                    animateColor(minutePicker, "textColor", getColor(R.color.textColor), Color.WHITE)
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

    private fun animateColor(view: View, propertyName: String, fromColor: Int, toColor: Int) {
        ObjectAnimator.ofObject(view, propertyName, ArgbEvaluator(), fromColor, toColor)
            .apply {
                duration = ANIMATE_TIME
                start()
            }
    }
}
package com.goldouble.android.workout

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.goldouble.android.workout.customView.CustomActionbar
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.dialog_number_picker.view.*
import java.text.DecimalFormat

class SettingsActivity : AppCompatActivity() {
    enum class Button(val key: String, val def: Int) {
        WORKOUT("workout_time", 5), REST("rest_time", 1),
        SET("set_number", 3), ROUND("round_number", 3),
        SETINTERVAL("set_interval", 3), ROUNDINTERVAL("round_interval", 3)
    }

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences("TimerSettings",Context.MODE_PRIVATE)

        CustomActionbar(this).apply {
            setToolbar()
            setTitle(R.string.timerSettings)
        }

        timerTypeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.text = getString(if(isChecked) R.string.countDown else R.string.countUp)
            prefs.edit().putBoolean("timer_type", isChecked).apply()
            setText()
        }

        workoutTimeBtn.setOnClickListener {
            pushButton(Button.WORKOUT)
        }

        restTimeBtn.setOnClickListener {
            pushButton(Button.REST)
        }

        setCardView.setOnClickListener {
            pushButton(Button.SET)
        }

        roundCardView.setOnClickListener {
            pushButton(Button.ROUND)
        }

        setIntervalBtn.setOnClickListener {
            pushButton(Button.SETINTERVAL)
        }

        roundIntervalBtn.setOnClickListener {
            pushButton(Button.ROUNDINTERVAL)
        }

        setText()
    }

    private fun setText() {
        prefs.apply {
            timerTypeSwitch.apply {
                isChecked = getBoolean("timer_type", true).also {
                    text = getString(if(it) R.string.countDown else R.string.countUp)
                }
            }

            val workoutTimeText = "${getInt("workout_time", Button.WORKOUT.def)}${getString(R.string.minute)}"
            workoutTimeBtn.text = workoutTimeText

            val restTimeText = "${getInt("rest_time", Button.REST.def)}${getString(R.string.minute)}"
            restTimeBtn.text = restTimeText

            setText.text = getInt("set_number", Button.SET.def).toString()
            roundText.text = getInt("round_number", Button.ROUND.def).toString()

            val setIntervalText = "${getInt("set_interval", Button.SETINTERVAL.def)}${getString(R.string.second)}"
            setIntervalBtn.text = setIntervalText

            val roundIntervalText = "${getInt("round_interval", Button.ROUNDINTERVAL.def)}${getString(R.string.second)}"
            roundIntervalBtn.text = roundIntervalText


            if (getBoolean("timer_type", true)) {
                val estimateTime = (getInt("workout_time", Button.WORKOUT.def) + getInt("rest_time", Button.REST.def)) *
                        getInt("set_number", Button.SET.def) * getInt("round_number", Button.ROUND.def) * 60
                val calTimeText = "${estimateTime / 60}:${DecimalFormat("00").format(estimateTime % 60)}"

                estimateTimeText.text = calTimeText
                workoutTimeBtn.isEnabled = true
            } else {
                estimateTimeText.text = "0:00"
                workoutTimeBtn.isEnabled = false
            }
        }
    }

    private fun pushButton(target: Button) {
        val numberPickerDialog = layoutInflater.inflate(R.layout.dialog_number_picker, null).apply {
            numberPicker.maxValue = 10
            numberPicker.minValue = 1
            numberPicker.value = prefs.getInt(target.key, target.def)
        }
        AlertDialog.Builder(this).setView(numberPickerDialog)
            .setPositiveButton(R.string.submit) { _, _ ->
                prefs.edit().putInt(target.key, numberPickerDialog.numberPicker.value).apply()
                setText()
            }.create().apply {
                setOnShowListener {
                    getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getColor(R.color.colorAccent))
                }
                show()
            }
    }
}
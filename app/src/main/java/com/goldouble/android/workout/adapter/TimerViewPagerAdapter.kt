package com.goldouble.android.workout.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.TimerActivity
import com.goldouble.android.workout.db.Logs
import io.realm.Realm
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_timer_cur_logs.view.*
import kotlinx.android.synthetic.main.activity_timer_timer.view.*
import kotlinx.android.synthetic.main.dialog_number_picker.view.*
import java.text.DecimalFormat
import java.util.*

class TimerViewPagerAdapter(val activity: TimerActivity) : RecyclerView.Adapter<TimerViewPagerAdapter.PagerViewHolder>() {
    enum class Workout { STOP, WORKOUT, REST }

    val layouts = listOf(
        R.layout.activity_timer_timer,
        R.layout.activity_timer_cur_logs
    )

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        return layouts[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimerViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bindData(position)
    }

    inner class PagerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val realm = Realm.getDefaultInstance()

        private var workoutedTime = 0
        var exTime = 0
        var setNum = 1
        var state = Workout.STOP

        fun bindData(position: Int) {
            when(layouts[position]) {
                R.layout.activity_timer_timer -> { //타이머
                    view.apply {
                        val defaultSetText = "1" + context.getString(R.string.set)
                        setLbl.text = defaultSetText

                        timerCardView.setOnClickListener {
                            when(state) {
                                Workout.STOP, Workout.REST ->
                                    startTimer(Workout.WORKOUT)
                                Workout.WORKOUT -> {
                                    activity.mTimer?.let { insertRecord() }
                                    startTimer(Workout.REST)
                                }
                            }
                        }
                        startBtn.setOnClickListener {
                            activity.mTimer?.let { insertRecord() }
                            startTimer(Workout.WORKOUT)
                        }
                        restBtn.setOnClickListener {
                            workoutedTime = exTime
                            startTimer(Workout.REST)
                        }
                        finishBtn.setOnClickListener {
                            activity.mTimer?.cancel()
                            activity.finish()
                        }
                        val defaultRestTime = context.getString(R.string.rest) + " 1" + context.getString(R.string.minute)
                        restTimeBtn.text = defaultRestTime
                        additionalInfoCardView.setOnClickListener {
                            val numberPickerDialog = activity.layoutInflater.inflate(R.layout.dialog_number_picker, null).apply {
                                numberPicker.maxValue = 10
                                numberPicker.minValue = 1
                                numberPicker.value = activity.rsTime / 60
                            }

                            AlertDialog.Builder(context).setView(numberPickerDialog)
                                .setPositiveButton(R.string.submit) { _, _ ->
                                    val selectedTime = numberPickerDialog.numberPicker.value
                                    val restTimeText = "${context.getString(R.string.rest)} ${selectedTime}${context.getString(R.string.minute)}"
                                    restTimeBtn.text = restTimeText
                                    activity.rsTime = selectedTime * 60
                                }
                                .show()
                        }
                    }
                }
                R.layout.activity_timer_cur_logs -> { //최근기록
                    realm.addChangeListener {
                        view.curLogRecyclerView.adapter = CurLogAdapter(it.where(Logs::class.java).findAll())
                        view.curLogRecyclerView.layoutManager = LinearLayoutManager(view.context)
                    }
                    view.curLogRecyclerView.adapter = CurLogAdapter(realm.where(Logs::class.java).findAll())
                    view.curLogRecyclerView.layoutManager = LinearLayoutManager(view.context)
                }
            }
        }

        private fun startTimer(workout: Workout) {
            val timerTask = object: TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        if(workout == Workout.WORKOUT) exTime++
                        else if (--exTime == 0) {
                            AlertDialog.Builder(view.context)
                                .setTitle(R.string.timer_restFinish_title)
                                .setMessage(R.string.timer_restFinish_content)
                                .setPositiveButton(R.string.submit) { _, _ -> }
                                .show()
                            activity.mTimer?.cancel()
                            insertRecord()
                        }
                        val timeText = "${exTime / 60}:${DecimalFormat("00").format(exTime % 60)}"
                        view.workoutTimeLbl.text = timeText
                    }
                }
            }
            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            exTime = if(workout == Workout.WORKOUT) -1 else activity.rsTime + 1
            activity.mTimer?.schedule(timerTask, 0, 1000)

            view.timerLayout.setBackgroundResource(if (workout == Workout.WORKOUT) R.drawable.cardview_workout_gradient else R.drawable.cardview_rest_gradient)

            state = workout

            view.restBtn.isEnabled = !view.restBtn.isEnabled
            view.startBtn.isEnabled = !view.startBtn.isEnabled
        }

        private fun insertRecord() {
            realm.beginTransaction()

            val record = realm.createObject<Logs>()
            record.apply {
                set = setNum++
                date = Calendar.getInstance().time
                workoutTime = workoutedTime
                restTime = activity.rsTime - exTime
            }

            realm.commitTransaction()

            val setText = "$setNum${view.context.getString(R.string.set)}"
            view.setLbl.text = setText
        }
    }
}
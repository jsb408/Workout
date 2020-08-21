package com.goldouble.android.workout.adapter

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.TimerActivity
import com.goldouble.android.workout.adapter.TimerViewPagerAdapter.Workout.*
import com.goldouble.android.workout.db.Logs
import io.realm.Realm
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_timer_cur_logs.view.*
import kotlinx.android.synthetic.main.activity_timer_timer.view.*
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class TimerViewPagerAdapter(val activity: TimerActivity) : RecyclerView.Adapter<TimerViewPagerAdapter.PagerViewHolder>() {
    enum class Workout(val color: Int, val title: Int, val phrase: Int) {
        STOP(R.color.timerStopped, R.string.stop, R.string.tapToStart),
        WORKOUT(R.color.timerStarted, R.string.workout, R.string.tapToPause),
        PAUSE(R.color.timerPaused, R.string.pause, R.string.tapToResume),
        REST(R.color.timerStarted, R.string.rest, R.string.tapToPause),
        INTERVAL(R.color.timerWaited, R.string.interval, R.string.tapToSkip)
    }

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
        private val prefs = activity.getSharedPreferences("TimerSettings",Context.MODE_PRIVATE)

        private var workoutedTime = 0
        var dfTime = 0
        var exTime = 0
        set(value) {
            field = value
            view.apply {
                val timeText = "${value / 60}:${DecimalFormat("00").format(value % 60)}"
                workoutTimeLbl.text = timeText
            }
        }

        private var setNum = 0
        set(value) {
            field = if (value > prefs.getInt("set_number", 3)) {
                roundNum++
                1
            } else value
            view.apply {
                val setText = "$field${context.getString(R.string.set)}"
                setLbl.text = setText
            }
        }

        private var roundNum = 0
        set(value) {
            field = if (value > prefs.getInt("round_number", 3)) 1 else value
            view.apply {
                val roundText = "$field${context.getString(R.string.round)}"
                roundLbl.text = roundText
            }
        }

        private var state = STOP
        set(value) {
            field = value
            view.apply {
                tapToLbl.text = context.getString(value.phrase)
                timerLayout.setBackgroundColor(context.getColor(value.color))
                pauseLbl.visibility = if(value == PAUSE) View.VISIBLE else {
                    workoutStatusLbl.text = context.getString(value.title)
                    View.GONE
                }
            }
        }

        fun bindData(position: Int) {
            when(layouts[position]) {
                R.layout.activity_timer_timer -> { //타이머
                    view.apply {
                        setNum = 1
                        roundNum = 1

                        exTime = if(prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time", 5) * 60 else 0

                        val restTimeText = "${prefs.getInt("rest_time", 1)}:00"
                        nextTimeLbl.text = restTimeText

                        val handler = Handler(Handler.Callback {
                            if (it.data.getString("click") == "Single") {
                                when (state) {
                                    STOP -> workout()
                                    WORKOUT -> timerPause()
                                    PAUSE -> state = if (workoutedTime > 0) REST else WORKOUT
                                    REST -> timerPause()
                                    INTERVAL -> Unit
                                }
                            } else { //더블탭
                                when(state) {
                                    PAUSE -> {
                                        if (workoutedTime == 0) {
                                            workoutedTime = if(prefs.getBoolean("timer_type", true))
                                                prefs.getInt("workout_time", 5) * 60 - exTime else exTime
                                            if(workoutedTime == 0) workoutedTime = 1
                                            rest()
                                        } else {
                                            insertRecord()
                                            workoutedTime = 0
                                            interval()
                                        }
                                    }
                                    INTERVAL -> workout()
                                    else -> Unit
                                }
                            }
                            true
                        })

                        val gestureDetector = GestureDetector(context, object: GestureDetector.OnGestureListener {
                            override fun onDown(e: MotionEvent?): Boolean {
                                return true
                            }
                            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                                return true
                            }
                            override fun onLongPress(e: MotionEvent?) = Unit
                            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                                return true
                            }
                            override fun onShowPress(e: MotionEvent?) = Unit
                            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                                val message = Message().apply {data = Bundle().apply { putString("click", "Single") } }
                                handler.sendMessageDelayed(message, 100)
                                return true
                            }
                        }).also {
                            it.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
                                override fun onDoubleTap(e: MotionEvent?): Boolean {
                                    handler.removeCallbacksAndMessages(null)

                                    val message = Message().apply {data = Bundle().apply { putString("click", "더블클릭") } }
                                    handler.sendMessage(message)

                                    return true
                                }

                                override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
                                    return true
                                }

                                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                                    return true
                                }
                            })
                        }

                        timerCardView.setOnTouchListener { v, event ->
                            if(event.action == MotionEvent.ACTION_UP) v.performClick()
                            gestureDetector.onTouchEvent(event)
                        }
                    }
                }
                R.layout.activity_timer_cur_logs -> { //최근기록
                    bindAdapter()
                    realm.addChangeListener { bindAdapter() }

                    val roundCount = "${view.curLogRecyclerView.adapter?.itemCount ?: 0}${view.context.getString(R.string.round)}"
                    view.roundText.text = roundCount
                }
            }
        }

        private fun timerPause() {
            state = PAUSE
        }

        private fun workout() {
            state = WORKOUT

            exTime = if (prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time", 5) * 60 else 0

            val countUpTask = object: TimerTask() {
                override fun run() {
                    if (state != PAUSE) {
                        activity.runOnUiThread {
                            exTime++
                        }
                    }
                }
            }

            val countDownTask = object: TimerTask() {
                override fun run() {
                    if (state != PAUSE) {
                        activity.runOnUiThread {
                            if (--exTime == 0) {
                                activity.mTimer?.cancel()
                                rest()
                            }
                        }
                    }
                }
            }

            view.apply {
                val restTime = "${prefs.getInt("rest_time", 1)}:00"

                nextStatusLbl.text = context.getString(R.string.rest)
                nextTimeLbl.text = restTime
            }

            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(if(prefs.getBoolean("timer_type", true)) countDownTask else countUpTask, 1000, 1000)
        }

        private fun rest() {
            state = REST

            exTime = prefs.getInt("rest_time", 1) * 60

            val countDownTask = object: TimerTask() {
                override fun run() {
                    if (state != PAUSE) {
                        activity.runOnUiThread {
                            if (--exTime == 0) {
                                activity.mTimer?.cancel()
                                insertRecord()
                                interval()
                            }
                        }
                    }
                }
            }

            view.apply {
                val workoutTime = "${prefs.getInt("workout_time", 5)}:00"

                nextStatusLbl.text = context.getString(R.string.workout)
                nextTimeLbl.text = workoutTime
            }

            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(countDownTask, 1000, 1000)
        }

        private fun interval() {
            state = INTERVAL

            exTime = prefs.getInt(if (setNum == 1) "round_interval" else "set_interval", 3)

            val countDownTask = object: TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        if (--exTime == 0) {
                            workout()
                        }
                    }
                }
            }

            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(countDownTask, 1000, 1000)
        }

        private fun insertRecord() {
            realm.beginTransaction()

            val record = realm.createObject<Logs>()
            record.apply {
                round = roundNum
                set = setNum++
                date = Calendar.getInstance().time
                workoutTime = workoutedTime
                restTime = prefs.getInt("rest_time", 1) * 60 - exTime
            }

            realm.commitTransaction()

            val setText = "$setNum ${view.context.getString(R.string.set)}"
            view.setLbl.text = setText
        }

        private fun bindAdapter() {
            val logsData = realm.where(Logs::class.java).findAll()
            val roundArray = arrayListOf<List<Logs>>()
            for (i in 1 .. logsData.last()!!.round)
                roundArray.add(logsData.filter {
                    val ldt = Instant.ofEpochMilli(it.date.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    ldt.month == LocalDate.now().month && ldt.dayOfMonth == LocalDate.now().dayOfMonth && it.round == i
                })

            view.curLogRecyclerView.adapter = CurLogsAdapter(roundArray)
            view.curLogRecyclerView.layoutManager = LinearLayoutManager(view.context)
        }
    }
}
package com.goldouble.android.workout.adapter

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.*
import androidx.core.content.res.ResourcesCompat
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
import java.text.DecimalFormat
import java.util.*

class TimerViewPagerAdapter(val activity: TimerActivity) : RecyclerView.Adapter<TimerViewPagerAdapter.PagerViewHolder>() {
    var logsView: View? = null

    //운동 기록을 임시저장하는 list 선언
    val roundArray = ArrayList<List<Logs>>() //round별 기록
        get() {
            field.clear()
            if (records.isNotEmpty())
                for (i in 1..records.last().round)
                    field.add(records.filter { it.round == i })
            return field
        }
    var records = ArrayList<Logs>() //운동 전체 기록

    //타이머 상태 enum(배경색, 제목, 상단텍스트, 하단텍스트)
    enum class Workout(val color: Int, val title: Int, val lowerPhrase: Int, val upperPharase: Int = R.string.blank) {
        STOP(R.color.timerStopped, R.string.blank, R.string.tapToStart),
        PREWORKOUT(R.color.timerPreWorkout, R.string.blank, R.string.blank, R.string.timer_starts_in),
        WORKOUT(R.color.timerWorkout, R.string.workout, R.string.tapToPause),
        PAUSE(R.color.timerPaused, R.string.workout, R.string.tapToResume, R.string.pause),
        REST(R.color.timerRest, R.string.rest, R.string.tapToPause),
        INTERVAL(R.color.timerWaited, R.string.interval, R.string.tapToSkip, R.string.next_round_starts_in),
        FINISH(R.color.timerResult, R.string.result, R.string.congratulation, R.string.finish)
    }

    //viewPager에 inflate 될 layout 목록
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
        var exTime = 0
        set(value) {
            field = value
            view.apply {
                workoutTimeLbl.text = timeText(value)
                workoutTimeLbl.textSize = 90f
                workoutTimeLbl.typeface = ResourcesCompat.getFont(context, R.font.jost_400_book)
            }
        }

        var singleTime = 0
        set(value) {
            field = value
            view.apply {
                workoutTimeLbl.text = value.toString()
                workoutTimeLbl.textSize = 120f
                workoutTimeLbl.typeface = ResourcesCompat.getFont(context, R.font.jost_500_medium)
            }
        }

        private var setNum = 1
        set(value) {
            field = if (value > prefs.getInt("set_number", 3)) {
                if (roundNum == prefs.getInt("round_number", 3)) {
                    activity.mTimer?.cancel()
                    workoutFinish()
                    setNum
                }
                else {
                    roundNum++
                    interval()
                    1
                }
            } else {
                workout()
                value
            }
            view.apply {
                val setText = "$field/${prefs.getInt("set_number", 3)} ${context.getString(R.string.set)}"
                setLbl.text = setText
            }
        }

        private var roundNum = 1
        set(value) {
            field = value
            view.apply {
                val roundText = "$field/${prefs.getInt("round_number", 3)} ${context.getString(R.string.round)}"
                roundLbl.text = roundText
            }
        }

        private var state = STOP
        set(value) {
            field = value
            view.apply {
                tapToLbl.text = context.getString(value.lowerPhrase)
                pauseLbl.text = context.getString(value.upperPharase)
                if(value != PAUSE) workoutStatusLbl.text = context.getString(value.title)
                timerLayout.setBackgroundColor(context.getColor(value.color))
            }
        }

        fun bindData(position: Int) {
            when(layouts[position]) {
                R.layout.activity_timer_timer -> { //타이머
                    view.apply {
                        exTime = 0

                        val roundText = "$roundNum/${prefs.getInt("round_number", 3)} ${context.getString(R.string.round)}"
                        roundLbl.text = roundText

                        val setText = "$setNum/${prefs.getInt("set_number", 3)} ${context.getString(R.string.set)}"
                        setLbl.text = setText

                        nextTimeLbl.text = timeText(prefs.getInt("workout_time", 5) * 60)

                        val handler = Handler {
                            if (it.data.getString("click") == "Single") {
                                when (state) {
                                    STOP -> preWorkout()
                                    WORKOUT -> timerPause()
                                    PAUSE -> state = if (workoutedTime > 0) REST else WORKOUT
                                    REST -> timerPause()
                                    else -> Unit
                                }
                            } else { //더블탭
                                when (state) {
                                    PAUSE -> {
                                        if (workoutedTime == 0) {
                                            workoutedTime = if (prefs.getBoolean("timer_type", true))
                                                    prefs.getInt("workout_time",5) * 60 - exTime else exTime
                                            if (workoutedTime == 0) workoutedTime = 1
                                            rest()
                                        } else {
                                            insertRecord()
                                            workoutedTime = 0
                                        }
                                    }
                                    INTERVAL -> workout()
                                    else -> Unit
                                }
                            }
                            true
                        }

                        //더블탭을 감지하기 위한 getureDetector
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
                                val message = Message().apply { data = Bundle().apply { putString("click", "Single") } }
                                handler.sendMessageDelayed(message, 100)
                                return true
                            }
                        }).also {
                            it.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
                                override fun onDoubleTap(e: MotionEvent?): Boolean {
                                    handler.removeCallbacksAndMessages(null)

                                    val message = Message().apply {data = Bundle().apply { putString("click", "Double") } }
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
                    logsView = view
                    bindAdapter()
                }
            }
        }

        private fun timerPause() {
            state = PAUSE
        }

        private fun preWorkout() {
            state = PREWORKOUT

            singleTime = 5

            val countDownTask = object: TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        if (--singleTime == 0) {
                            workout()
                        }
                    }
                }
            }

            //타이머 세팅
            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(countDownTask, 1000, 1000)
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
                                workoutedTime = prefs.getInt("workout_time", 5) * 60
                                rest()
                            }
                        }
                    }
                }
            }

            view.apply {
                nextStatusLbl.text = context.getString(R.string.rest)
                nextTimeLbl.text = timeText(prefs.getInt("rest_time", 1) * 60)
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
                            }
                        }
                    }
                }
            }

            view.apply {
                if (setNum == prefs.getInt("set_number", 3)) {
                    nextStatusLbl.text = context.getString(R.string.interval)
                    nextTimeLbl.text = timeText(prefs.getInt("round_interval", 3))
                } else {
                    nextStatusLbl.text = context.getString(R.string.workout)
                    nextTimeLbl.text = timeText(prefs.getInt("workout_time", 5) * 60)
                }
            }

            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(countDownTask, 1000, 1000)
        }

        private fun interval() {
            state = INTERVAL

            singleTime = prefs.getInt("round_interval", 3)

            val countDownTask = object: TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        if (--singleTime == 0) {
                            workout()
                        }
                    }
                }
            }

            view.apply {
                nextStatusLbl.text = context.getString(R.string.workout)
                nextTimeLbl.text = timeText(prefs.getInt("workout_time", 5) * 60)
            }

            activity.mTimer?.cancel()
            activity.mTimer = Timer()
            activity.mTimer!!.schedule(countDownTask, 1000, 1000)
        }

        private fun insertRecord() {
            records.add(
                Logs(
                    workoutTime = workoutedTime,
                    restTime = prefs.getInt("rest_time", 1) * 60 - exTime,
                    round = roundNum,
                    set = setNum++,
                    date = Calendar.getInstance().time
                )
            )

            bindAdapter()
        }

        private fun workoutFinish() {
            state = FINISH

            view.apply {
                roundLbl.visibility = View.INVISIBLE
                setLbl.visibility = View.INVISIBLE

                val finishText = "${setNum}SETS\n${roundNum}ROUNDS"
                workoutTimeLbl.text = finishText
                workoutTimeLbl.textSize = 60f
                workoutTimeLbl.typeface = ResourcesCompat.getFont(context, R.font.jost_500_medium)

                nextLbl.text = context.getString(R.string.total)
                nextStatusLbl.text = context.getString(R.string.workoutTime)
            }

            insertRealm()
        }

        private fun bindAdapter() {
            activity.mCurLogsAdapter = CurLogsAdapter(roundArray)

            view.apply {
                logsView?.curLogRecyclerView?.adapter = activity.mCurLogsAdapter
                logsView?.curLogRecyclerView?.layoutManager = LinearLayoutManager(context)
            }

            val roundCount = "${activity.mCurLogsAdapter?.itemCount ?: 0} ${view.context.getString(R.string.round)}"
            logsView?.roundText?.text = roundCount

            val setCount = "${prefs.getInt("set_number", 3)} ${view.context.getString(R.string.set)}"
            logsView?.setText?.text = setCount
        }

        private fun insertRealm() {
            records.forEach {
                realm.beginTransaction()

                val record = realm.createObject<Logs>()
                record.apply {
                    round = it.round
                    workoutTime = it.workoutTime
                    restTime = it.restTime
                    set = it.set
                    date = it.date
                }

                realm.commitTransaction()
            }
        }

        private fun timeText(second: Int): String {
            val formatter = DecimalFormat("00")
            return "${formatter.format(second / 60)}:${formatter.format(second % 60)}"
        }
    }
}
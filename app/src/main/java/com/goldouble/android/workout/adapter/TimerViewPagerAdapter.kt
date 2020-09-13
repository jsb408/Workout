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
    val records = ArrayList<Logs>() //운동 전체 기록

    var seq = 0L
    var logsView: View? = null

    //타이머 상태 enum(배경색, 제목, 상단텍스트, 하단텍스트)
    enum class Workout(val color: Int, val title: Int, val lowerPhrase: Int, val upperParase: Int = R.string.blank) {
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

        private var workoutedTime: Int? = null
        var exTime = 0
        set(value) {
            field = value
            view.apply {
                workoutTimeLbl.apply {
                    text = timeText(value)
                    textSize = 90f
                    typeface = ResourcesCompat.getFont(context, R.font.jost_400_book)
                }
            }
        }

        var singleTime = 0
        set(value) {
            field = value
            view.apply {
                workoutTimeLbl.apply {
                    text = value.toString()
                    textSize = 120f
                    typeface = ResourcesCompat.getFont(context, R.font.jost_500_medium)
                }
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
                    setTimer(INTERVAL)
                    1
                }
            } else {
                setTimer(WORKOUT)
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
                pauseLbl.text = context.getString(value.upperParase)
                if(value != PAUSE) workoutStatusLbl.text = context.getString(value.title)
                timerLayout.setBackgroundColor(context.getColor(value.color))
            }
        }

        fun bindData(position: Int) {
            when(layouts[position]) {
                R.layout.activity_timer_timer -> { //타이머
                    view.apply {
                        exTime = 0
                        Log.d("WIDTH", timerCardView.width.toString())
                        //timerCardView.layoutParams = ConstraintLayout.LayoutParams(timerCardView.width, timerCardView.width)

                        val roundText = "$roundNum/${prefs.getInt("round_number", 3)} ${context.getString(R.string.round)}"
                        roundLbl.text = roundText

                        val setText = "$setNum/${prefs.getInt("set_number", 3)} ${context.getString(R.string.set)}"
                        setLbl.text = setText

                        nextTimeLbl.text = timeText(prefs.getInt("workout_time", 5) * 60)

                        val handler = Handler {
                            if (it.data.getString("click") == "Single") {
                                when (state) {
                                    STOP -> {
                                        seq = System.currentTimeMillis()
                                        setTimer(PREWORKOUT)
                                    }
                                    WORKOUT -> setTimer(PAUSE)
                                    PAUSE -> state = if (workoutedTime != null) REST else WORKOUT
                                    REST -> setTimer(PAUSE)
                                    else -> Unit
                                }
                            } else { //더블탭
                                when (state) {
                                    PAUSE -> {
                                        if (workoutedTime == null) {
                                            workoutedTime = if (prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time",5) * 60 - exTime else exTime
                                            setTimer(REST)
                                        } else {
                                            insertRecord()
                                            workoutedTime = null
                                        }
                                    }
                                    INTERVAL -> setTimer(WORKOUT)
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

                    val roundCount = "0 ${view.context.getString(R.string.round)}"
                    logsView?.roundText?.text = roundCount

                    val setCount = "0 ${view.context.getString(R.string.set)}"
                    logsView?.setText?.text = setCount

                    bindAdapter()
                }
            }
        }

        private fun setTimer(workout: Workout) {
            state = workout

            if(state != PAUSE) {
                when (state) {
                    PREWORKOUT -> singleTime = 5
                    WORKOUT -> {
                        exTime = if (prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time", 5) * 60 else 0
                        view.apply {
                            nextStatusLbl.text = context.getString(R.string.rest)
                            nextTimeLbl.text = timeText(prefs.getInt("rest_time", 1) * 60)
                        }
                    }
                    REST -> {
                        exTime = prefs.getInt("rest_time", 1) * 60
                        view.apply {
                            if (setNum == prefs.getInt("set_number", 3)) {
                                nextStatusLbl.text = context.getString(R.string.interval)
                                nextTimeLbl.text = timeText(prefs.getInt("round_interval", 3))
                            } else {
                                nextStatusLbl.text = context.getString(R.string.workout)
                                nextTimeLbl.text = timeText(if(prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time", 5) * 60 else 0)
                            }
                        }
                    }
                    INTERVAL -> {
                        singleTime = prefs.getInt("round_interval", 3)
                        view.apply {
                            nextStatusLbl.text = context.getString(R.string.workout)
                            nextTimeLbl.text = timeText(if(prefs.getBoolean("timer_type", true)) prefs.getInt("workout_time", 5) * 60 else 0)
                        }
                    }
                    else -> Unit
                }

                val timerTask = object : TimerTask() {
                    override fun run() {
                        activity.runOnUiThread {
                            when (state) {
                                PREWORKOUT, INTERVAL -> {
                                    if (--singleTime == 0) {
                                        setTimer(WORKOUT)
                                    }
                                }
                                WORKOUT -> {
                                    if (state != PAUSE) {
                                        if (prefs.getBoolean("timer_type", true)) {
                                            if (--exTime == 0) {
                                                activity.mTimer?.cancel()
                                                workoutedTime = prefs.getInt("workout_time", 5) * 60
                                                setTimer(REST)
                                            }
                                        } else exTime++
                                    }
                                }
                                REST -> {
                                    if (state != PAUSE) {
                                        if (--exTime == 0) {
                                            activity.mTimer?.cancel()
                                            insertRecord()
                                        }
                                    }
                                }
                                else -> Unit
                            }
                        }
                    }
                }

                activity.mTimer?.cancel()
                activity.mTimer = Timer()
                activity.mTimer!!.schedule(timerTask, 1000, 1000)
            }
        }

        private fun insertRecord() {
            records.add(
                Logs(
                    workoutTime = workoutedTime!!,
                    restTime = prefs.getInt("rest_time", 1) * 60 - exTime,
                    round = roundNum,
                    set = setNum,
                    date = Calendar.getInstance().time
                )
            )

            setNum++
            logsView?.noDataLbl?.visibility = View.GONE
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
                nextTimeLbl.text = timeText(records.sumBy { it.workoutTime + it.restTime })
            }

            insertRealm()
        }

        private fun bindAdapter() {
            activity.mCurLogsAdapter = CurLogsAdapter(records.groupBy { it.round }.values.toList())

            view.apply {
                val roundCount = "$roundNum ${view.context.getString(R.string.round)}"
                logsView?.roundText?.text = roundCount

                val setCount = "$setNum ${view.context.getString(R.string.set)}"
                logsView?.setText?.text = setCount

                logsView?.curLogRecyclerView?.adapter = activity.mCurLogsAdapter
                logsView?.curLogRecyclerView?.layoutManager = LinearLayoutManager(context)
            }
        }
        
        //Realm 저장
        private fun insertRealm() {
            records.forEach {
                realm.beginTransaction()

                val record = realm.createObject<Logs>()
                record.apply {
                    workoutSeq = seq
                    round = it.round
                    workoutTime = it.workoutTime
                    restTime = it.restTime
                    set = it.set
                    date = it.date
                }

                realm.commitTransaction()
            }
        }

        //숫자 형식 지정
        private fun timeText(second: Int): String {
            val formatter = DecimalFormat("00")
            return "${formatter.format(second / 60)}:${formatter.format(second % 60)}"
        }
    }
}
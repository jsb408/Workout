package com.goldouble.android.workout.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.Logs
import com.goldouble.android.workout.R
import io.realm.Realm
import io.realm.kotlin.createObject
import kotlinx.android.synthetic.main.activity_timer_cur_logs.view.*
import kotlinx.android.synthetic.main.activity_timer_timer.view.*
import java.text.DecimalFormat
import java.util.*

class TimerViewPagerAdapter(val activity: Activity) : RecyclerView.Adapter<TimerViewPagerAdapter.PagerViewHolder>() {
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

        var mTimer : Timer? = null
        var exTime = 0

        fun bindData(position: Int) {
            when(layouts[position]) {
                R.layout.activity_timer_timer -> { //타이머
                    view.startBtn.setOnClickListener {
                        controlTimer(true)
                    }
                    view.restBtn.setOnClickListener {
                        controlTimer(false)
                    }
                    view.finishBtn.setOnClickListener {
                        mTimer?.cancel()
                        activity.finish()
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

        private fun controlTimer(isStart: Boolean) {
            val timerTask = object: TimerTask() {
                override fun run() {
                    activity.runOnUiThread {
                        if(isStart) exTime++ else if (--exTime == 0) {
                            AlertDialog.Builder(view.context)
                                .setTitle(R.string.timer_restFinish_title)
                                .setMessage(R.string.timer_restFinish_content)
                                .setPositiveButton(R.string.submit) { _, _ -> }
                                .show()
                            mTimer?.cancel()
                        }
                        val timeText = "${exTime / 60}:${DecimalFormat("00").format(exTime % 60)}"
                        view.timeLbl.text = timeText
                    }
                }
            }

            mTimer?.cancel()
            mTimer = object : Timer() {
                override fun cancel() {
                    insertRecord(isStart)
                    super.cancel()
                }
            }
            exTime = if(isStart) -1 else 61
            mTimer?.schedule(timerTask, 0, 1000)

            view.restBtn.isEnabled = !view.restBtn.isEnabled
            view.startBtn.isEnabled = !view.startBtn.isEnabled
        }

        private fun insertRecord(isStart: Boolean) {
            realm.beginTransaction()

            val record = realm.createObject<Logs>()
            record.apply {
                date = Calendar.getInstance().time
                time = if(isStart) exTime else 60
                isWorkout = isStart
            }

            realm.commitTransaction()
        }
    }
}
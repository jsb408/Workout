package com.goldouble.android.workout.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.goldouble.android.workout.LogsActivity
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_logs_layout.view.*
import java.util.*

class LogsViewPagerAdapter(val activity: LogsActivity) : RecyclerView.Adapter<LogsViewPagerAdapter.PagerViewHolder>() {
    enum class DateScope { TODAY, WEEK, MONTH }

    override fun getItemCount(): Int = DateScope.values().size

    override fun getItemViewType(position: Int): Int {
        return R.layout.activity_logs_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bindData(DateScope.values()[position])
    }

    inner class PagerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val realm = Realm.getDefaultInstance()

        fun bindData(scope: DateScope) {
            view.apply {
                val logs = realm.where(Logs::class.java).findAll()

                val cal = Calendar.getInstance()

                val monthLog = arrayListOf<Logs>()
                val weekLog = arrayListOf<Logs>()

                for (year in cal.apply { time = logs.first()!!.date }.get(Calendar.YEAR) .. cal.apply { time = logs.last()!!.date }.get(Calendar.YEAR)) {
                    val yearTmpLogs = logs.filter { cal.apply { it.date }.get(Calendar.YEAR) == year }
                    for (month in cal.apply { time = yearTmpLogs.first()!!.date }.get(Calendar.MONTH)..cal.apply { time = yearTmpLogs.last()!!.date }.get(Calendar.MONTH)) {
                        val monthTmpLogs = yearTmpLogs.filter { cal.apply { it.date }.get(Calendar.MONTH) == month }
                        monthLog.add(monthTmpLogs.reduce{ acc, log -> Logs(
                            round = log.round, set = log.set, date = log.date,
                            workoutTime = acc.workoutTime + log.workoutTime,
                            restTime = acc.restTime + log.restTime
                        )})
                        for (week in cal.apply { time = monthTmpLogs.first()!!.date }.get(Calendar.WEEK_OF_MONTH) .. cal.apply { time = monthTmpLogs.last()!!.date }.get(Calendar.WEEK_OF_MONTH)) {
                            val weekTmpLogs = monthTmpLogs.filter { cal.apply { it.date }.get(Calendar.WEEK_OF_MONTH) == week }
                            Log.d("WEEK", weekTmpLogs.joinToString())
                            if (weekTmpLogs.isNotEmpty()) {
                                Log.d("WEEK", weekTmpLogs.joinToString())
                                weekLog.add(weekTmpLogs.reduce { acc, log -> Logs(
                                    round = log.round, set = log.set, date = log.date,
                                    workoutTime = acc.workoutTime + log.workoutTime,
                                    restTime = acc.restTime + log.restTime
                                )})
                            }
                        }
                    }
                }

                roundNumText.text = logs.last()!!.round.toString()
                setNumText.text = logs.size.toString()

                chartViewPager.adapter = ChartViewPagerAdapter(when(scope) {
                    DateScope.TODAY -> logs
                    DateScope.WEEK -> weekLog
                    DateScope.MONTH -> monthLog
                }, scope)
                chartViewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                TabLayoutMediator(chartTabLayout, chartViewPager) { _, _ -> Unit }.attach()

                chartTabLayout.getTabAt(0)?.text = context.getString(R.string.workout)
                chartTabLayout.getTabAt(1)?.text = context.getString(R.string.rest)
            }
        }
    }
}
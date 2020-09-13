package com.goldouble.android.workout.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.goldouble.android.workout.R
import com.goldouble.android.workout.customView.CustomBarChartRender
import com.goldouble.android.workout.db.Logs
import io.realm.Realm
import io.realm.exceptions.RealmMigrationNeededException
import kotlinx.android.synthetic.main.activity_logs_record.view.*
import kotlinx.android.synthetic.main.activity_logs_stats.view.*
import kotlinx.android.synthetic.main.list_item_record.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogsViewPagerAdapter : RecyclerView.Adapter<LogsViewPagerAdapter.PagerViewHolder>() {
    val layouts = listOf(
        R.layout.activity_logs_record,
        R.layout.activity_logs_stats
    )

    override fun getItemCount(): Int = layouts.size

    override fun getItemViewType(position: Int): Int {
        return layouts[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bindData(position)
    }

    inner class PagerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val realm = Realm.getDefaultInstance()
        private val logs = combineLog()

        fun bindData(position: Int) {
            when (layouts[position]) {
                R.layout.activity_logs_record -> {
                    view.apply {
                        recordRecyclerView.adapter = RecordAdapter(logs)
                        recordRecyclerView.layoutManager = LinearLayoutManager(view.context)
                    }
                }
                R.layout.activity_logs_stats -> {
                    view.apply {
                        setChart()
                        Log.d("CHART", "LOG")
                        logBarChart.setOnChartValueSelectedListener(object: OnChartValueSelectedListener {
                            override fun onValueSelected(e: Entry, h: Highlight) {
                                logBarChart.highlightValues(arrayOf(
                                    Highlight(h.x, h.y, h.xPx, h.yPx, h.dataSetIndex, 0, YAxis.AxisDependency.LEFT),
                                    Highlight(h.x, h.y, h.xPx, h.yPx, h.dataSetIndex, 1, YAxis.AxisDependency.LEFT)
                                ))
                                setInfo(logs[h.x.toInt()])
                                Log.d("Entry", e.toString())
                                Log.d("Hightlight", h.toString())
                            }

                            override fun onNothingSelected() {
                                chartDetail.visibility = View.GONE
//                                ValueAnimator.ofInt(chartDetail.measuredHeight, -200).apply {
//                                    addUpdateListener {
//                                        val value = it.animatedValue as Int
//                                        val layoutParams = chartDetail.layoutParams
//                                        layoutParams.height = value
//                                        chartDetail.layoutParams = layoutParams
//                                    }
//                                    addListener( object: AnimatorListenerAdapter() {
//                                        override fun onAnimationEnd(animation: Animator?) {
//                                            super.onAnimationEnd(animation)
//                                            chartDetail.visibility = View.GONE
//                                        }
//                                    })
//                                    duration = 500
//                                    start()
//                                }
                            }
                        })
                    }
                }
            }
        }

        private fun combineLog(): List<Logs> {
            val data = ArrayList<Logs>()

            try {
                val logs = realm.where(Logs::class.java).findAll()

                logs.groupBy { it.workoutSeq }
                    .forEach {
                        val totalWorkout = it.value.sumBy { log -> log.workoutTime }
                        val totalRest = it.value.sumBy { log -> log.restTime }

                        data.add(
                            Logs(
                                workoutSeq = it.value.last().workoutSeq,
                                workoutTime = totalWorkout,
                                restTime = totalRest,
                                set = it.value.last().set,
                                round = it.value.last().round,
                                date = Date(it.value.last().workoutSeq)
                            )
                        )
                    }
            } catch (e: RealmMigrationNeededException) {
                e.printStackTrace()
                realm.deleteAll()
                data.addAll(combineLog())
            } finally {
                return data
            }
        }

        private fun setChart() {
            view.logBarChart.apply {
                setScaleEnabled(false)
                isDoubleTapToZoomEnabled = false
                axisLeft.apply {
                    axisMinimum = 0f
                    textColor = resources.getColor(R.color.subTextColor, null)
                    axisLineWidth = 0f
                    textSize = 12f
                }
                axisRight.isEnabled = false
                legend.apply {
                    orientation = Legend.LegendOrientation.VERTICAL
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    verticalAlignment = Legend.LegendVerticalAlignment.TOP
                    form = Legend.LegendForm.LINE
                    formSize = 27f
                    formLineWidth = 8f
                    textColor = resources.getColor(R.color.textColor, null)
                    setDrawInside(true)
                    yOffset = -10f
                }
                xAxis.apply {
                    this.position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1.0f
                    textColor = resources.getColor(R.color.subTextColor, null)
                    textSize = 12f
                }

                renderer = CustomBarChartRender(this, this.animator, this.viewPortHandler).apply { setRadius(30f) }

                description.isEnabled = false

                setChartData()
            }
        }

        private fun setChartData() {
            val times = ArrayList<BarEntry>()
            val labelList = ArrayList<String>()

            logs.forEach {
                times.add(BarEntry(labelList.size.toFloat(), floatArrayOf(it.workoutTime / 60f, it.restTime / 60f)))
                labelList.add(SimpleDateFormat("MM.dd HH:mm", Locale.getDefault()).format(it.date))
            }

            view.logBarChart.apply {
                data = BarData().apply {
                    addDataSet(BarDataSet(times, null).apply {
                        colors = listOf(context.resources.getColor(R.color.chart1, null), context.resources.getColor(R.color.chart2, null))
                        stackLabels = arrayOf(context.getString(R.string.workout), context.getString(R.string.rest))
                    })
                    setValueTextSize(0f)
                }
                xAxis.apply {
                    valueFormatter = object: IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return labelList[value.toInt()]
                        }
                    }
                }
            }
        }

        private fun setInfo(log: Logs) {
            view.apply {
                val totalTime = log.workoutTime + log.restTime
                val formatter = DecimalFormat("00")
                val tTimeText = "${formatter.format(totalTime / 60 / 60)}:${formatter.format(totalTime / 60 % 60)}:${formatter.format(totalTime % 60)}"

                totalTimeText.text = tTimeText
                recordDateText.text = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault()).format(log.date)
                workoutText.text = (log.workoutTime / 60).toString()
                restText.text = (log.restTime / 60).toString()
                setText.text = log.set.toString()
                roundText.text = log.round.toString()

                chartDetail.visibility = View.VISIBLE

//                chartDetail.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                val target = chartDetail.measuredHeight
//                val layoutParam = chartDetail.layoutParams
//                layoutParam.height = 0
//                chartDetail.layoutParams = layoutParam
//
//                chartDetail.visibility = View.VISIBLE
//                ValueAnimator.ofInt(chartDetail.measuredHeight, target).apply {
//                    addUpdateListener {
//                        val layoutParams = chartDetail.layoutParams
//                        layoutParams.height = (target * it.animatedFraction).toInt()
//                        chartDetail.layoutParams = layoutParams
//                    }
//                    duration = 500
//                    start()
//                }
            }
        }
    }
}
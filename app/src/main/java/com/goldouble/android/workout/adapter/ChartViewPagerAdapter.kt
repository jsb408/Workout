package com.goldouble.android.workout.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.goldouble.android.workout.R
import com.goldouble.android.workout.customView.CustomBarChartRender
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.activity_logs_layout_chart.view.*
import java.text.SimpleDateFormat
import java.util.*

class ChartViewPagerAdapter(val logs: List<Logs>, val scope: LogsViewPagerAdapter.DateScope) : RecyclerView.Adapter<ChartViewPagerAdapter.PagerViewHolder>() {
    var setNum = 0
    var roundNum = 0

    enum class ChartData { WORKOUT, REST }

    override fun getItemCount(): Int = ChartData.values().size

    override fun getItemViewType(position: Int): Int {
        return R.layout.activity_logs_layout_chart
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChartViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: ChartViewPagerAdapter.PagerViewHolder, position: Int) {
        holder.bindData(ChartData.values()[position])
    }

    inner class PagerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val dateList = ArrayList<String>()
        val labelList = ArrayList<String>()

        fun bindData(chart: ChartData) {
            val tmpDateList = ArrayList<String>()
            logs.forEach {
                tmpDateList.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date))
            }
            dateList.addAll(tmpDateList.distinct().sortedDescending())

//        dateSpinner.apply {
//            adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, dateList)
//            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                    setChartData(dateList[position])
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
//            }
//        }

            var totalTime = 0
            logs.forEach { totalTime += if(chart == ChartData.WORKOUT) it.workoutTime else it.restTime }
            val totalText = "${totalTime / 60}${view.context.getString(R.string.minute)} ${totalTime % 60}${view.context.getString(R.string.second)}"

            view.totalTimeText.text = totalText

            view.logBarChart.apply {
                setScaleEnabled(false)
                setTouchEnabled(false)
                axisLeft.axisMinimum = 0.0f
                axisRight.isEnabled = false
                legend.isEnabled = false
                xAxis.apply {
                    this.position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1.0f
                }

                val barChartRender = CustomBarChartRender(this, this.animator, this.viewPortHandler).apply { setRadius(30f) }
                renderer = barChartRender

                description.isEnabled = false
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP

                setChartData(dateList[0], chart)
            }
        }

        private fun setChartData(date: String, chart: ChartData) {
            val times = ArrayList<BarEntry>()

            labelList.clear()
            logs.filter {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == date
            }.also {
                setNum = it.size
                roundNum = it.last().round
                Log.d("NUMS", "SET : $setNum / ROUND : $roundNum")
            }.forEach {
                times.add(BarEntry(labelList.size.toFloat(), floatArrayOf((if(chart == ChartData.WORKOUT) it.workoutTime else it.restTime).toFloat())))
                //labelList.add("${it.set}${view.context.getString(R.string.set)}")
                labelList.add(SimpleDateFormat(
                    when(scope) {
                        LogsViewPagerAdapter.DateScope.TODAY -> "HH:mm"
                        LogsViewPagerAdapter.DateScope.WEEK -> "MM.dd"
                        LogsViewPagerAdapter.DateScope.MONTH -> "MMM"
                    }, Locale.getDefault()).format(it.date))
            }

            view.logBarChart.apply {
                data = BarData().apply {
                    addDataSet(BarDataSet(times, null).apply {
                        colors = listOf(context.resources.getColor(R.color.buttonColor, null))
                        //stackLabels = arrayOf(view.context.getString(R.string.workout))
                    })
                    setValueTextSize(9f)
                    setValueFormatter(object: ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if(value >= 60f) "${(value / 60).toInt()}${view.context.getString(R.string.minute)} ${(value % 60).toInt()}${view.context.getString(R.string.second)}"
                            else value.toInt().toString() + view.context.getString(R.string.second)
                        }
                    })
                }
                xAxis.apply {
                    valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return labelList[value.toInt()]
                        }
                    }
                }

                invalidate()
            }
        }
    }
}
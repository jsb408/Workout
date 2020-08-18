package com.goldouble.android.workout.adapter

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
import com.goldouble.android.workout.LogsActivity
import com.goldouble.android.workout.R
import com.goldouble.android.workout.customView.CustomBarChartRender
import com.goldouble.android.workout.db.Logs
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_logs_layout.view.*
import java.text.SimpleDateFormat
import java.util.*

class LogsViewPagerAdapter(val activity: LogsActivity) : RecyclerView.Adapter<LogsViewPagerAdapter.PagerViewHolder>() {
    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int {
        return R.layout.activity_logs_layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogsViewPagerAdapter.PagerViewHolder {
        return PagerViewHolder(LayoutInflater.from(parent.context).inflate(viewType, parent, false))
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.bindData(position)
    }

    inner class PagerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val realm = Realm.getDefaultInstance()

        val dateList = ArrayList<String>()
        lateinit var logs: RealmResults<Logs>
        val labelList = ArrayList<String>()

        fun bindData(position: Int) {
            logs = realm.where(Logs::class.java).findAll().apply {
                val tmpDateList = ArrayList<String>()
                forEach {
                    tmpDateList.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date))
                }
                dateList.addAll(tmpDateList.distinct().sortedDescending())
            }

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

            view.logBarChart.apply {
                setScaleEnabled(false)
                axisLeft.axisMinimum = 0.0f
                axisRight.isEnabled = false
                xAxis.apply {
                    //position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1.0f
                }

                val barChartRender = CustomBarChartRender(this, this.animator, this.viewPortHandler).apply { setRadius(30f) }
                renderer = barChartRender

                description.isEnabled = false
                legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP

                //setChartData(dateList[0])
            }
        }

        private fun setChartData(date: String) {
            val times = ArrayList<BarEntry>()

            labelList.clear()
            logs.filter {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == date
            }.forEach {
                times.add(BarEntry(labelList.size.toFloat(), floatArrayOf(it.workoutTime.toFloat(), it.restTime.toFloat())))
                labelList.add("${it.set}${view.context.getString(R.string.set)}")
            }

            view.logBarChart.apply {
                data = BarData().apply {
                    addDataSet(BarDataSet(times, null).apply {
                        colors = listOf(getColor(R.color.buttonColor), getColor(R.color.buttonColor))
                        stackLabels = arrayOf(view.context.getString(R.string.workout), view.context.getString(R.string.rest))
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
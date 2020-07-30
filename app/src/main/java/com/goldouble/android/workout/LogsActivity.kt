package com.goldouble.android.workout

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.goldouble.android.workout.db.Logs
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_logs.*
import kotlinx.android.synthetic.main.activity_timer_timer.*
import kotlinx.android.synthetic.main.dialog_number_picker.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.github.mikephil.charting.listener.ChartTouchListener as ChartTouchListener1

class LogsActivity : AppCompatActivity() {
    private val realm = Realm.getDefaultInstance()
    private lateinit var logs: RealmResults<Logs>
    val labelList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        val dateList = ArrayList<String>()

        logs = realm.where(Logs::class.java).findAll().apply {
            val tmpDateList = ArrayList<String>()
            forEach {
                tmpDateList.add(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date))
            }
            dateList.addAll(tmpDateList.distinct().sortedDescending())
        }

        dateSpinner.apply {
            adapter = ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, dateList)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    setChartData(dateList[position])
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }

        logBarChart.apply {
            setScaleEnabled(false)
            axisLeft.axisMinimum = 0.0f
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1.0f
            }

            description.isEnabled = false
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.main_logBtnLbl)

        setChartData(dateList[0])
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                finish()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setChartData(date: String) {
        val times = ArrayList<BarEntry>()

        labelList.clear()
        logs.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == date
        }.forEach {
            times.add(BarEntry(labelList.size.toFloat(), floatArrayOf(it.workoutTime.toFloat(), it.restTime.toFloat())))
            labelList.add("${it.set}${getString(R.string.set)}")
        }

        logBarChart.apply {
            data = BarData().apply {
                addDataSet(BarDataSet(times, null).apply {
                    colors = listOf(Color.RED, Color.BLUE)
                    stackLabels = arrayOf(getString(R.string.workout), getString(R.string.rest))
                })
                setValueTextSize(9f)
                setValueFormatter(object: ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if(value >= 60f) "${(value / 60).toInt()}${getString(R.string.minute)} ${(value % 60).toInt()}${getString(R.string.second)}"
                            else value.toInt().toString() + getString(R.string.second)
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
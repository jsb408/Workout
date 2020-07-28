package com.goldouble.android.workout

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_logs.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        logLineChart.apply {
            setScaleEnabled(false)
            axisRight.setDrawLabels(false)
            description.isEnabled = false
        }

        setChartData(dateList[0])
    }

    private fun setChartData(date: String) {
        val workoutTimes = ArrayList<Entry>()
        val restTimes = ArrayList<Entry>()

        labelList.clear()
        logs.filter {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.date) == date
        }.forEach {
            (if(it.isWorkout) workoutTimes else restTimes).add(Entry(labelList.size.toFloat(), it.time.toFloat()))
            labelList.add(SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(it.date))
        }

        logLineChart.apply {
            data = LineData().apply {
                addDataSet(LineDataSet(workoutTimes, getString(R.string.workout)).apply {
                    color = Color.RED
                    circleColors = listOf(Color.RED)
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    lineWidth = 2.5f
                })
                addDataSet(LineDataSet(restTimes, getString(R.string.rest)).apply {
                    color = Color.BLUE
                    circleColors = listOf(Color.BLUE)
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    lineWidth = 2.5f
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
                setLabelCount(labelList.size, true)
            }

            invalidate()
        }
    }
}
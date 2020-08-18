package com.goldouble.android.workout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.goldouble.android.workout.adapter.LogsViewPagerAdapter
import com.goldouble.android.workout.customView.CustomActionbar
import com.goldouble.android.workout.customView.CustomBarChartRender
import com.goldouble.android.workout.db.Logs
import com.google.android.material.tabs.TabLayoutMediator
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

        CustomActionbar(this).apply {
            setToolbar()
            setTitle(R.string.main_logBtnLbl)
        }

        viewPager.adapter = LogsViewPagerAdapter(this)
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> Unit}.attach()
        TabLayoutMediator(tabLayoutIndicator, viewPager) { _, _ -> Unit}.attach()

        tabLayout.getTabAt(0)?.text = "TODAY"
        tabLayout.getTabAt(1)?.text = "WEEK"
        tabLayout.getTabAt(2)?.text = "MONTH"
    }
}
package com.goldouble.android.workout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.goldouble.android.workout.adapter.LogsViewPagerAdapter
import com.goldouble.android.workout.customView.CustomActionbar
import com.goldouble.android.workout.db.Logs
import com.google.android.material.tabs.TabLayoutMediator
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_logs.*
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

        viewPager.adapter = LogsViewPagerAdapter()
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> Unit}.attach()

        tabLayout.getTabAt(0)?.text = "RECORD"
        tabLayout.getTabAt(1)?.text = "STATS"
    }
}
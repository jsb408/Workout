package com.goldouble.android.workout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Realm.init(this)
        MobileAds.initialize(this)

        adView.loadAd(AdRequest.Builder().build())

        timerBtn.setOnClickListener {
            startActivity(Intent(this, TimerActivity::class.java))
        }

        logsBtn.setOnClickListener {
            startActivity(Intent(this, LogsActivity::class.java))
        }

        sleepBtn.setOnClickListener {
            startActivity(Intent(this, BedtimeActivity::class.java))
        }
    }
}
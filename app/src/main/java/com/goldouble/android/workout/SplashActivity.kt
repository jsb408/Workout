package com.goldouble.android.workout

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        supportActionBar?.hide()

        try {
            Handler().postDelayed({
                startActivity(Intent(application, MainActivity::class.java))
                finish()
            }, 1500)
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() = Unit
}
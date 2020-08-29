package com.goldouble.android.workout.customView

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.actionbar_custom.*
import kotlinx.android.synthetic.main.actionbar_custom.view.*

class CustomActionbar(private val activity: AppCompatActivity) {
    private val toolbar = activity.toolbar

    fun setToolbar() {
        activity.setSupportActionBar(toolbar)
        activity.backButton.setOnClickListener {
            activity.finish()
        }
    }

    fun setTitle(resId: Int) {
        toolbar.titleText.text = activity.getString(resId)
    }

    fun setBackgroundColor(color: Int) {
        toolbar.toolbarLayout.setBackgroundColor(color)
    }

    fun setTextColor(color: Int) {
        toolbar.titleText.setTextColor(color)
        toolbar.backButton.setColorFilter(color)
    }
}
package com.goldouble.android.workout.adapter

import android.content.Context
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.activity_timer_cur_logs.view.*
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*
import java.text.SimpleDateFormat
import java.util.*

class CurLogsAdapter(val data : ArrayList<List<Logs>>) : RecyclerView.Adapter<CurLogsAdapter.ItemViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_logs, parent, false)
        return ItemViewHolder(adapterView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindLogsData(data[position])
    }

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindLogsData(data: List<Logs>?) {
            data?.let {
                view.apply {
                    val roundCount = "${data[0].round}${when (data[0].round % 10) {1 -> "st"; 2 -> "nd"; 3 -> "rd"; else -> "th" }}"
                    roundCountText.text = roundCount

                    data.forEach {
                        val linearLayout = LinearLayout(context)
                        linearLayout.apply {
                            val numText = "${context.getString(R.string.set)} ${it.set}"
                            addView(textView(context, numText))
                            addView(textView(context, timeText(context, it.workoutTime)))
                            addView(textView(context, timeText(context, it.restTime)))
                            addView(textView(context, SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(it.date)))
                        }
                        logLayout.addView(linearLayout)
                    }
                }
            }
        }

        private fun textView(context: Context, content: String): TextView {
            val params = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f)
            return TextView(context).apply {
                text = content
                layoutParams = params
                gravity = CENTER
                textSize = 18f
            }
        }

        private fun timeText(context: Context, time: Int): String {
            return "${time / 60}${context.getString(R.string.minute)} ${time % 60}${context.getString(R.string.second)}"
        }
    }
}
package com.goldouble.android.workout.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.list_item_cur_log.view.*
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*
import java.text.SimpleDateFormat
import java.util.*

class CurLogAdapter(val data : List<Logs>) : RecyclerView.Adapter<CurLogAdapter.ItemViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_log, parent, false)
        return ItemViewHolder(adapterView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindLogsData(data[position])
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindLogsData(data: Logs?) {
            data?.let {
                view.apply {
                    val numText = "${context.getString(R.string.set)} ${data.set}"
                    setNumText.text = numText
                    workoutTimeText.text = timeText(context, data.workoutTime)
                    restTimeText.text = timeText(context, data.restTime)
                    recordTimeText.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(data.date)
                }
            }
        }
    }

    fun timeText(context: Context, time: Int): String {
        return "${time / 60}${context.getString(R.string.minute)} ${time % 60}${context.getString(R.string.second)}"
    }
}
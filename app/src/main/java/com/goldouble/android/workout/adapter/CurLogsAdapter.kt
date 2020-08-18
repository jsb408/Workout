package com.goldouble.android.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.activity_timer_cur_logs.view.*
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*

class CurLogsAdapter(val data: ArrayList<List<Logs>>) : RecyclerView.Adapter<CurLogsAdapter.ItemViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_logs, parent, false)
        return ItemViewHolder(adapterView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindLogsData(data[position])
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindLogsData(data: List<Logs>?) {
            data?.let {
                view.apply {
                    logsRecyclerView.apply {
                        adapter = CurLogAdapter(data)
                        layoutManager = LinearLayoutManager(view.context)
                    }
                }
            }
        }
    }
}
package com.goldouble.android.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*
import java.util.ArrayList

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

                    logsRecyclerView.apply {
                        adapter = CurLogAdapter(data)
                        layoutManager = LinearLayoutManager(view.context)
                    }
                }
            }
        }
    }
}
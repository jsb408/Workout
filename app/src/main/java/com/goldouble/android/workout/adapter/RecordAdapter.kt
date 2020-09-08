package com.goldouble.android.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.R
import com.goldouble.android.workout.db.Logs
import kotlinx.android.synthetic.main.list_item_record.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class RecordAdapter(private val data: List<Logs>): RecyclerView.Adapter<RecordAdapter.ItemViewHolder>() {
    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_record, parent, false)
        return ItemViewHolder(adapterView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindData(log: Logs?) {
            log?.let {
                view.apply {
                    val totalTime = log.workoutTime + log.restTime
                    val formatter = DecimalFormat("00")
                    val tTimeText = "${formatter.format(totalTime / 60)}:${formatter.format(totalTime % 60)}"

                    totalTimeText.text = tTimeText
                    recordDateText.text = SimpleDateFormat("yy.MM.dd HH:mm", Locale.getDefault()).format(log.date)
                    workoutText.text = (log.workoutTime / 60).toString()
                    restText.text = (log.restTime / 60).toString()
                    setText.text = log.set.toString()
                    roundText.text = log.round.toString()
                }
            }
        }
    }
}
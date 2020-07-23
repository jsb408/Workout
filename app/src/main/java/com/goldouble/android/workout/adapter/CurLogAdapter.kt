package com.goldouble.android.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.Logs
import com.goldouble.android.workout.R
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import kotlinx.android.synthetic.main.list_item_cur_logs.view.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class CurLogAdapter(data: OrderedRealmCollection<Logs>) : RealmRecyclerViewAdapter<Logs, CurLogAdapter.ItemViewHolder>(data, true) {
    private val item = data.filter {
        val ldt = Instant.ofEpochMilli(it.date.time).atZone(ZoneId.systemDefault()).toLocalDateTime()
        ldt.month == LocalDate.now().month && ldt.dayOfMonth == LocalDate.now().dayOfMonth
    }

    override fun getItemCount(): Int = item.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_cur_logs, parent, false)
        return ItemViewHolder(adapterView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bindLogsData(item[position])
    }

    inner class ItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindLogsData(data: Logs?) {
            data?.let {
                val timeText = "${it.time / 60}${view.context.getString(R.string.minute)} ${it.time % 60}${view.context.getString(R.string.second)}"
                view.apply {
                    isWorkoutLbl.text = view.context.getString(if (it.isWorkout) R.string.workout else R.string.rest)
                    timeLbl.text = timeText
                    dateLbl.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(it.date)
                }
            }
        }
    }
}
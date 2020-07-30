package com.goldouble.android.workout.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goldouble.android.workout.db.Logs
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
                view.apply {
                    val setText = "${it.set} ${view.context.getString(R.string.set)}"
                    setLbl.text = setText
                    workoutTimeLbl.text = timeText(it.workoutTime)
                    restTimeLbl.text = timeText(it.restTime)
                    dateLbl.text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(it.date)
                }
            }
        }

        fun timeText(time: Int): String {
            return "${time / 60}${view.context.getString(R.string.minute)} ${time % 60}${view.context.getString(R.string.second)}"
        }
    }
}
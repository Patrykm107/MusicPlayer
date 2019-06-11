package com.example.musicplayer


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.util.*
import java.util.concurrent.TimeUnit


class MainAdapter(private val context:Context, private val values: ArrayList<Song>) : RecyclerView.Adapter<MainAdapter.ViewHolder>() {

    override fun getItemCount(): Int = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.card_elem_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = values[position]
        holder.titleText?.text = currentItem.name
        val millis = currentItem.duration

        holder.durationText?.text = String.format("%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
        )

        holder.itemView.setOnClickListener {
            (context as MainActivity).reset()
            context.changeSong(position)
            context.play()
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var titleText: TextView? = null
        var durationText: TextView? = null

        init {
            titleText = itemView.findViewById(R.id.titleText)
            durationText = itemView.findViewById(R.id.durationText)
        }

    }

}
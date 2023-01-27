package com.example.tracker

import android.graphics.Movie
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter

class CustomAdapter(private val dataSet: ArrayList<String>, listener : onItemClickListener) :
    RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private val listener : onItemClickListener = listener

    interface onItemClickListener{

        fun onItemClick(position: Int)
    }

    /*fun setOnItemClickListener(mlistener : onItemClickListener) {
        listener=mlistener
    }*/

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.recycler, viewGroup, false)

        return ViewHolder(view, listener)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.textView.text = dataSet[position]
        viewHolder.itemView.setOnClickListener {
            listener.onItemClick(position)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() : Int { return dataSet.size}

    class ViewHolder(view: View , listener : onItemClickListener) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)

        init {
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}
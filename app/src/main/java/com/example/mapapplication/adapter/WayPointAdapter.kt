package com.example.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.mapapplication.common.BaseAdapter
import com.example.mapapplication.databinding.ItemWaypointBinding

class WayPointAdapter : BaseAdapter<WayPintData, ItemWaypointBinding>() {

    private var onItemClicked: ((WayPintData) -> Unit)? = null
    private var onTextClicked: ((WayPintData) -> Unit)? = null

    fun setOnItemClicked(onItemClicked: (WayPintData) -> Unit) {
        this.onItemClicked = onItemClicked
    }
    fun setOnTextClicked(onTextClick: (WayPintData) -> Unit) {
        this.onTextClicked = onTextClick
    }

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemWaypointBinding =
        ItemWaypointBinding.inflate(inflater, parent, false)

    override fun ItemWaypointBinding.onBindItem(item: WayPintData, position: Int) {
        waypoint.setText(item.description)
        count.text = (position+1).toString()

        clearPoint.setOnClickListener { onItemClicked?.invoke(item) }
        waypoint.setOnClickListener { onTextClicked?.invoke(item) }
    }
}
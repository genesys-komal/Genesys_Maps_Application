package com.example.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.example.mapapplication.common.BaseAdapter
import com.example.mapapplication.databinding.ManeuversItemBinding
import com.example.mapapplication.originCode.net.models.RouteManeuver

class ManeuverAdapter : BaseAdapter<RouteManeuver, ManeuversItemBinding>() {

    private var onItemClicked: ((RouteManeuver) -> Unit)? = null
    fun setOnItemClicked(onItemClicked: (RouteManeuver) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ManeuversItemBinding =
        ManeuversItemBinding.inflate(inflater, parent, false)

    override fun ManeuversItemBinding.onBindItem(item: RouteManeuver, position: Int) {
        Steps.text = "Step "+(position + 1).toString()+": "
        stepDescription.text = item.instruction
        if (item.toll == true){
            tollRoute.isVisible = true
            tollRoute.text = "Toll: Yes"
        }
        root.setOnClickListener { setOnItemClicked { item } }


        if (item.time==0.0){
            timeLength.text = "Time: ${item.time} , Length : ${item.length}"
        }else{
            timeLength.text = "Time: " +getMinTime(item.time.toInt())  + ", Length : " + String.format("%.2f", convertAndRoundOff(item.length))
        }


    }
    private fun convertAndRoundOff(meters: Double): Double {
        val kilometers = meters / 1000

        return String.format("%.2f", meters).toDouble()
    }
    private fun getMinTime(totalSecs: Int): String {
        val hours = totalSecs / 3600
        val minutes = (totalSecs % 3600) / 60
        val seconds = totalSecs % 60
        if (hours > 0) return "$hours h $minutes m $seconds s"
        else return "$minutes m $seconds s"
    }
}


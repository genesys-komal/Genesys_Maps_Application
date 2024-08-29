package com.example.mapapplication.adapter

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mapapplication.originCode.net.models.PlaceAutocompletePrediction
import com.example.mapapplication.R

class AutoCompleteAdapter(
    private val mContext: Context,
    private val mLayoutResourceId: Int,
    cities: List<PlaceAutocompletePrediction>
) : ArrayAdapter<PlaceAutocompletePrediction>(mContext, mLayoutResourceId, cities) {
    private val city: MutableList<PlaceAutocompletePrediction> = ArrayList(cities)
    private var allCities: List<PlaceAutocompletePrediction> = ArrayList(cities)
    private var onClicked: ((PlaceAutocompletePrediction) -> Unit)? = null

    override fun getCount(): Int {
        return city.size
    }
    override fun getItem(position: Int): PlaceAutocompletePrediction {
        return city[position]
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setItemClicked(onItemClicked: (PlaceAutocompletePrediction) -> Unit) {
        this.onClicked = onItemClicked
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = (mContext as Activity).layoutInflater
            convertView = inflater.inflate(mLayoutResourceId, parent, false)
        }
        try {
            val placeAutocompletePrediction: PlaceAutocompletePrediction = getItem(position)
            val fullName = convertView!!.findViewById<View>(R.id.text_view_fullname) as TextView
            val name = convertView!!.findViewById<View>(R.id.text_view_name) as TextView
            fullName.text = placeAutocompletePrediction.structuredFormatting.secondaryText
            name.text = placeAutocompletePrediction.structuredFormatting.mainText
            convertView.setOnClickListener {
                onClicked?.invoke(placeAutocompletePrediction)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return convertView!!
    }
}
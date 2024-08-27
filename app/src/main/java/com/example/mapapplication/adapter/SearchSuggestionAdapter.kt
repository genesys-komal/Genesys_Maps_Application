package com.example.mapapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.mapapplication.originCode.net.models.PlaceAutocompletePrediction
import com.example.mapapplication.common.BaseAdapter
import com.example.mapapplication.databinding.ItemSearchSuggestionBinding

class SearchSuggestionAdapter : BaseAdapter<PlaceAutocompletePrediction, ItemSearchSuggestionBinding>() {

    private var onItemClicked: ((PlaceAutocompletePrediction) -> Unit)? = null
    private var directionClicked: ((PlaceAutocompletePrediction) -> Unit)? = null

    fun setOnItemClicked(onItemClicked: (PlaceAutocompletePrediction) -> Unit) {
        this.onItemClicked = onItemClicked
    }
    fun getDirectionClicked(itemClicked: (PlaceAutocompletePrediction) -> Unit) {
        this.directionClicked = itemClicked
    }

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemSearchSuggestionBinding =
        ItemSearchSuggestionBinding.inflate(inflater, parent, false)

    override fun ItemSearchSuggestionBinding.onBindItem(item: PlaceAutocompletePrediction, position: Int) {
        val shortAddress = item.structuredFormatting.secondaryText
        val address = item.structuredFormatting.mainText
        textViewName.text = address
        distance.text = String.format("%.1f",(item.distanceMeters.toDouble()/1000)).toDouble().toString()+"km"
        textViewFullname.text = shortAddress
        root.setOnClickListener { onItemClicked?.invoke(item) }
//        getDirection.setOnClickListener { directionClicked?.invoke(item) }
    }
}
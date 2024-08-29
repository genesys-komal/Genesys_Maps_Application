package com.example.mapapplication.adapter

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.mapapplication.common.BaseAdapter
import com.example.mapapplication.databinding.CategoryItemBinding
import com.example.mapapplication.models.CategoryList


class CategoryAdapter : BaseAdapter<CategoryList, CategoryItemBinding>() {

    private var onItemClicked: ((CategoryList) -> Unit)? = null
    // Variable to keep track of the selected item position
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION
    fun setOnItemClicked(onItemClicked: (CategoryList) -> Unit) {
        this.onItemClicked = onItemClicked
    }

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): CategoryItemBinding =
        CategoryItemBinding.inflate(inflater, parent, false)

    override fun CategoryItemBinding.onBindItem(item: CategoryList, position: Int) {

        catIcon.setImageDrawable(item.drawable)
        catName.text = item.name
        // Set background color based on the selected position
        if (position == selectedItemPosition) {
            root.setCardBackgroundColor(Color.LTGRAY) // Highlight selected item
        } else {
            root.setCardBackgroundColor(Color.WHITE) // Default color for non-selected items
        }

        root.setOnClickListener {
            if (selectedItemPosition == position) {
                selectedItemPosition = -1
                val cat = CategoryList("",item.drawable)
                onItemClicked?.invoke(cat)

            } else {
                selectedItemPosition = position
                onItemClicked?.invoke(item)
            }
            notifyDataSetChanged()

        }
    }
}
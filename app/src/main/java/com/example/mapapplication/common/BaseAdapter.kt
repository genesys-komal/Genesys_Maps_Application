package com.example.mapapplication.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T : Any, B : ViewBinding>(
    areItemsTheSame: (oldItem: T, newItem: T) -> Boolean = { oldItem, newItem -> oldItem == newItem },
    areContentsTheSame: (oldItem: T, newItem: T) -> Boolean = { _, _ -> false },
    getChangePayload: (oldItem: T, newItem: T) -> Any? = { _, _ -> null },
    diffUtil: DiffUtil.ItemCallback<T> = getDefaultItemCallback(
        areItemsTheSame,
        areContentsTheSame,
        getChangePayload
    )
) : ListAdapter<T, BaseAdapter.ViewHolder<B>>(diffUtil) {

   private var onSubmit: (() -> Unit)? = null

    abstract fun inflate(inflater: LayoutInflater, parent: ViewGroup): B
    abstract fun B.onBindItem(item: T, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<B> =
        ViewHolder(inflate(LayoutInflater.from(parent.context), parent))

    override fun onBindViewHolder(holder: ViewHolder<B>, position: Int) {
        holder.binding.onBindItem(getItem(position), position)
    }

    override fun submitList(list: List<T>?) {
        super.submitList(list)
        onSubmit?.invoke()
    }

    class ViewHolder<B : ViewBinding>(val binding: B) : RecyclerView.ViewHolder(binding.root)
}

fun <T : Any> getDefaultItemCallback(
    areItemsTheSame: (T, T) -> Boolean,
    areContentsTheSame: (T, T) -> Boolean,
    getChangePayload: (T, T) -> Any?
) = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = areItemsTheSame(oldItem, newItem)
    override fun areContentsTheSame(oldItem: T, newItem: T) = areContentsTheSame(oldItem, newItem)
    override fun getChangePayload(oldItem: T, newItem: T) = getChangePayload(oldItem, newItem)
}
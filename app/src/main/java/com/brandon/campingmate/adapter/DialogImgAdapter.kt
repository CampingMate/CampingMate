package com.brandon.campingmate.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemMapImgBinding
import com.bumptech.glide.Glide

class DialogImgAdapter : ListAdapter<String, DialogImgAdapter.DialogImgHolder>(diffCallback){

//var imgItem: List<String> = listOf()

    inner class DialogImgHolder(private val binding: ItemMapImgBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String) {
            with(binding) {

                Glide.with(itemView)
                    .load(item)
                    .into(ivDiaglogimg)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogImgHolder {
        val inflater =
            parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ItemMapImgBinding.inflate(inflater, parent, false)
        return DialogImgHolder(binding)
    }

    override fun onBindViewHolder(holder: DialogImgHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun clear() {
        submitList(null)
    }

    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }
}
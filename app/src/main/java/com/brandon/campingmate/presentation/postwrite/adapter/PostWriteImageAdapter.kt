package com.brandon.campingmate.presentation.postwrite.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brandon.campingmate.databinding.ItemPostwriteImageBinding
import com.bumptech.glide.Glide

class PostWriteImageAdapter(private val onImageDeleteClicked: (Uri) -> Unit) :
    ListAdapter<Uri, PostWriteImageAdapter.ViewHolder>(object : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
            return true
        }
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostwriteImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPostwriteImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(imageUri: Uri) {
            with(binding) {
                Glide.with(binding.root).load(imageUri).into(binding.ivImage)
                btnClose.setOnClickListener {
                    onImageDeleteClicked(imageUri)
                }
            }
        }

    }
}